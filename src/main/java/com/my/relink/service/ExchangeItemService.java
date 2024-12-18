package com.my.relink.service;

import com.my.relink.chat.service.ChatService;
import com.my.relink.controller.exchangeItem.dto.req.CreateExchangeItemReqDto;
import com.my.relink.controller.exchangeItem.dto.req.UpdateExchangeItemReqDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetAllExchangeItemsRespDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetExchangeItemRespDto;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.category.repository.CategoryRepository;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.repository.PointRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExchangeItemService {

    private final ExchangeItemRepository exchangeItemRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final UserTrustScoreService userTrustScoreService;
    private final TradeService tradeService;
    private final ImageService imageService;
    private final LikeService likeService;
    private final ChatService chatService;

    @Transactional
    public long createExchangeItem(CreateExchangeItemReqDto reqDto, Long userId) {
        Category category = getValidCategory(reqDto.getCategoryId());
        User user = getValidUser(userId);
        validateDeposit(reqDto.getDeposit(), userId);
        ExchangeItem exchangeItem = reqDto.toEntity(category, user);
        return exchangeItemRepository.save(exchangeItem).getId();
    }

    public GetExchangeItemRespDto getExchangeItemsByUserId(Long userId, int page, int size) {
        User user = getValidUser(userId);
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<ExchangeItem> items = exchangeItemRepository.findByUserId(user.getId(), pageable);
        if (items.isEmpty()) {
            return GetExchangeItemRespDto.empty(pageable);
        }

        List<Long> itemIds = items.getContent().stream().map(ExchangeItem::getId).toList();
        Map<Long, Trade> tradeMap = tradeService.getTradesByItemIds(itemIds);
        Map<Long, String> imageMap = imageService.getFirstImagesByItemIds(EntityType.EXCHANGE_ITEM, itemIds);

        Page<GetExchangeItemRespDto> content = items.map(item -> GetExchangeItemRespDto.from(item, tradeMap, imageMap));

        return GetExchangeItemRespDto.of(content);
    }

    public GetExchangeItemRespDto getExchangeItemModifyPage(Long itemId, Long userId) {
        ExchangeItem exchangeItem = findByIdOrFail(itemId);
        exchangeItem.validExchangeItemOwner(exchangeItem.getUser().getId(), userId);

        return GetExchangeItemRespDto.from(exchangeItem);
    }

    public GetAllExchangeItemsRespDto getAllExchangeItems(String search, String deposit, TradeStatus tradeStatus, Long categoryId, int page, int size) {
        Category category = getValidCategory(categoryId);
        Pageable pageable = PageRequest.of(page, size);

        if (deposit != null && !deposit.isEmpty() && !deposit.equalsIgnoreCase("asc") && !deposit.equalsIgnoreCase("desc")) {
            throw new BusinessException(ErrorCode.INVALID_SORT_PARAMETER);
        }

        Page<ExchangeItem> itemsPage = exchangeItemRepository.findAllByCriteria(search, tradeStatus, category, deposit, pageable);
        List<Long> itemIds = itemsPage.getContent().stream().map(ExchangeItem::getId).toList();
        Map<Long, String> imageMap = imageService.getFirstImagesByItemIds(EntityType.EXCHANGE_ITEM, itemIds);

        Page<GetAllExchangeItemsRespDto> content = itemsPage.map(item -> {
            int trustScore = userTrustScoreService.getTrustScore(item.getUser());
            return GetAllExchangeItemsRespDto.fromAllItems(item, imageMap, trustScore);
        });

        return GetAllExchangeItemsRespDto.of(content);
    }

    public GetAllExchangeItemsRespDto getExchangeItemFromOwner(Long itemId, Long userId) {
        ExchangeItem exchangeItem = findByIdOrFail(itemId);
        validExchangeItemTradeStatus(exchangeItem.getTradeStatus());
        int trustScore = userTrustScoreService.getTrustScore(exchangeItem.getUser());
        List<String> imageUrls = imageService.getImageUrlsByItemId(EntityType.EXCHANGE_ITEM, itemId);
        Boolean like = likeService.existsItemLike(itemId, userId);
        return GetAllExchangeItemsRespDto.fromItem(exchangeItem, imageUrls, trustScore, like);
    }

    @Transactional
    public Long updateExchangeItem(Long itemId, UpdateExchangeItemReqDto reqDto, Long userId) {
        ExchangeItem exchangeItem = findByIdOrFail(itemId);
        validExchangeItemTradeStatus(exchangeItem.getTradeStatus());
        Category category = getValidCategory(reqDto.getCategoryId());
        validateDeposit(reqDto.getDeposit(), userId);
        exchangeItem.update(
                reqDto.getName(),
                reqDto.getDescription(),
                category,
                reqDto.getItemQuality(),
                reqDto.getSize(),
                reqDto.getBrand(),
                reqDto.getDesiredItem(),
                reqDto.getDeposit()
        );
        return exchangeItem.getId();
    }

    // 삭제는 soft delete
    @Transactional
    public Long deleteExchangeItem(Long itemId, Long userId) {
        ExchangeItem exchangeItem = findByIdOrFail(itemId);
        exchangeItem.validExchangeItemOwner(exchangeItem.getUser().getId(), userId);
        validDeleteExchangeItemTradeStatus(exchangeItem.getTradeStatus());
        exchangeItem.delete();
        deleteRelatedEntities(exchangeItem.getId());
        return exchangeItem.getId();
    }

    // 연관된 image, like, chat 삭제
    private void deleteRelatedEntities(Long itemId) {
        imageService.deleteImagesByEntityId(EntityType.EXCHANGE_ITEM, itemId);
        likeService.deleteLikesByExchangeItemId(itemId);
        Long tradeId = tradeService.getTradeIdByItemId(itemId);
        chatService.deleteChatsByTradeId(tradeId);
    }

    // 상품의 거래 상태 확인(수정 시)
    public void validExchangeItemTradeStatus(TradeStatus tradeStatus) {
        if (tradeStatus != TradeStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.ITEM_NOT_AVAILABLE);
        }
    }

    // 상품의 거래 상태 확인(삭제 시)
    public void validDeleteExchangeItemTradeStatus(TradeStatus tradeStatus) {
        if (tradeStatus == TradeStatus.IN_EXCHANGE) {
            throw new BusinessException(ErrorCode.ITEM_IN_EXCHANGE);
        }
    }

    // user 가져오기
    public User getValidUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    // category 가져오기
    public Category getValidCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    // 보증금 유효성 검사
    public void validateDeposit(Integer deposit, Long userId) {
        // 보증금이 0보다 작은 경우
        if (deposit < 0) {
            throw new BusinessException(ErrorCode.DEPOSIT_CANNOT_LESS_ZERO);
        }
        // 포인트가 없거나 포인트가 보증금보다 적은 경우
        Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_NOT_FOUND));
        if (point.getAmount() < deposit) {
            throw new BusinessException(ErrorCode.POINT_SHORTAGE);
        }
    }

    public ExchangeItem findByIdOrFail(Long itemId) {
        return exchangeItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXCHANGE_ITEM_NOT_FOUND));
    }
}
