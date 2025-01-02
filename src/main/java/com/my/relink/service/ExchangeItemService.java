package com.my.relink.service;

import com.my.relink.controller.exchangeItem.dto.req.ChoiceExchangeItemReqDto;
import com.my.relink.controller.exchangeItem.dto.req.CreateExchangeItemReqDto;
import com.my.relink.controller.exchangeItem.dto.req.GetAllExchangeItemReqDto;
import com.my.relink.controller.exchangeItem.dto.req.UpdateExchangeItemReqDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetAllExchangeItemsRespDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetExchangeItemRespDto;
import com.my.relink.controller.trade.dto.response.TradeIdRespDto;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.category.repository.CategoryRepository;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.repository.PointRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.util.MetricConstants;
import io.micrometer.core.annotation.Timed;
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
@Timed(MetricConstants.SERVICE_EXCHANGE_ITEM_TIME)
public class ExchangeItemService {

    private final ExchangeItemRepository exchangeItemRepository;
    private final CategoryRepository categoryRepository;
    private final PointRepository pointRepository;
    private final UserTrustScoreService userTrustScoreService;
    private final TradeService tradeService;
    private final ImageService imageService;
    private final LikeService likeService;
    private final UserService userService;

    @Transactional
    public long createExchangeItem(CreateExchangeItemReqDto reqDto, Long userId) {
        Category category = getValidCategory(reqDto.getCategoryId());
        User user = userService.findByIdOrFail(userId);
        validateDeposit(reqDto.getDeposit(), userId);
        ExchangeItem exchangeItem = reqDto.toEntity(category, user);
        return exchangeItemRepository.save(exchangeItem).getId();
    }

    public GetExchangeItemRespDto getExchangeItemsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ExchangeItem> items = exchangeItemRepository.findByUserId(userId, pageable);
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

    public GetAllExchangeItemsRespDto getAllExchangeItems(GetAllExchangeItemReqDto reqDto) {
        Category category = (reqDto.getCategoryId() != null) ? getValidCategory(reqDto.getCategoryId()) : null;
        Pageable pageable = PageRequest.of(reqDto.getPage(), reqDto.getSize());
        Page<ExchangeItem> itemsPage = exchangeItemRepository.findAllByCriteria(reqDto.getSearch(),
                reqDto.getTradeStatus(),
                category,
                reqDto.getDeposit(),
                pageable);
        List<Long> itemIds = itemsPage.getContent().stream().map(ExchangeItem::getId).toList();
        Map<Long, String> imageMap = imageService.getFirstImagesByItemIds(EntityType.EXCHANGE_ITEM, itemIds);

        Page<GetAllExchangeItemsRespDto> content = itemsPage.map(item -> {
            int trustScore = userTrustScoreService.getTrustScore(item.getUser());
            return GetAllExchangeItemsRespDto.fromAllItems(item, imageMap, trustScore);
        });

        return GetAllExchangeItemsRespDto.of(content);
    }

    public GetAllExchangeItemsRespDto getExchangeItemFromOwner(Long itemId, Long userId) {
        ExchangeItem exchangeItem = findByIdFetchUser(itemId);
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

    public GetExchangeItemRespDto getExchangeItemChoicePage(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ExchangeItem> items = exchangeItemRepository.findAvailableItemsByUserId(userId, pageable);
        if (items.isEmpty()) {
            return GetExchangeItemRespDto.empty(pageable);
        }
        List<Long> itemIds = items.getContent().stream().map(ExchangeItem::getId).toList();
        Map<Long, String> imageMap = imageService.getFirstImagesByItemIds(EntityType.EXCHANGE_ITEM, itemIds);
        Page<GetExchangeItemRespDto> content = items.map(item -> GetExchangeItemRespDto.from(item, imageMap));

        return GetExchangeItemRespDto.of(content);
    }

    public TradeIdRespDto choiceExchangeItem(Long itemId, ChoiceExchangeItemReqDto reqDto, Long requesterId) {
        ExchangeItem itemFromOwner = findByIdOrFail(itemId);
        ExchangeItem itemFromRequester = findByIdFetchUser(reqDto.getItemId());
        User requester = userService.findByIdOrFail(requesterId);
        itemFromRequester.validExchangeItemOwner(itemFromRequester.getUser().getId(), requesterId);
        validExchangeItemTradeStatus(itemFromOwner.getTradeStatus());
        return tradeService.createTrade(itemFromOwner, itemFromRequester, requester);
    }

    // 삭제는 soft delete
    @Transactional
    public Long deleteExchangeItem(Long itemId, Long userId) {
        ExchangeItem exchangeItem = findByIdOrFail(itemId);
        exchangeItem.validExchangeItemOwner(exchangeItem.getUser().getId(), userId);
        validDeleteExchangeItemTradeStatus(exchangeItem.getTradeStatus());
        exchangeItem.delete();
        deleteRelatedEntities(exchangeItem.getId(), userId);
        return exchangeItem.getId();
    }

    private void deleteRelatedEntities(Long itemId, Long userId) {
        tradeService.deleteTrade(itemId, userId);
        likeService.deleteLikes(itemId);
        imageService.deleteImages(EntityType.EXCHANGE_ITEM, itemId);
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


    public ExchangeItem findByIdFetchUser(Long itemId) {
        return exchangeItemRepository.findByIdWithUser(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXCHANGE_ITEM_NOT_FOUND));
    }

}
