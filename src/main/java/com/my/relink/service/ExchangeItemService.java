package com.my.relink.service;

import com.my.relink.controller.exchangeItem.dto.req.CreateExchangeItemReqDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetExchangeItemRespDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetExchangeItemsByUserRespDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetExchangeItemsAndPageByUserRespDto;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.category.repository.CategoryRepository;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.repository.ImageRepository;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.repository.PointRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.trade.repository.TradeRepository;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExchangeItemService {

    private final ExchangeItemRepository exchangeItemRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final TradeRepository tradeRepository;
    private final ImageRepository imageRepository;

    public long createExchangeItem(CreateExchangeItemReqDto reqDto, Long userId) {
        Category category = getValidCategory(reqDto.getCategoryId());
        User user = getValidUser(userId);
        // 보증금에 대한 유효성 검사
        validateDeposit(reqDto, userId);
        ExchangeItem exchangeItem = reqDto.toEntity(category, user);
        return exchangeItemRepository.save(exchangeItem).getId();
    }

    public GetExchangeItemsAndPageByUserRespDto getExchangeItemsByUserId(Long userId, int page, int size) {
        User user = getValidUser(userId);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ExchangeItem> items = exchangeItemRepository.findByUserId(user.getId(), pageable);
        // 내 교환상품 목록이 비어있는 경우 빈 결과 반환
        if (items.isEmpty()) {
            return GetExchangeItemsAndPageByUserRespDto.builder()
                    .content(List.of())
                    .pageInfo(GetExchangeItemsAndPageByUserRespDto.PageInfo.builder()
                            .totalElements(0)
                            .totalPages(0)
                            .hasPrevious(false)
                            .hasNext(false)
                            .build())
                    .build();
        }
        List<Long> itemIds = items.getContent().stream().map(ExchangeItem::getId).toList();
        List<Trade> trades = tradeRepository.findByExchangeItemIds(itemIds);
        Map<Long, Trade> tradeMap = trades.stream()
                .flatMap(trade -> List.of(
                        Map.entry(trade.getOwnerExchangeItem().getId(), trade),
                        Map.entry(trade.getRequesterExchangeItem().getId(), trade)
                ).stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        List<Image> images = imageRepository.findImages(EntityType.EXCHANGE_ITEM, itemIds);
        Map<Long, String> imageMap = images.stream()
                .collect(Collectors.toMap(Image::getEntityId, Image::getImageUrl));
        List<GetExchangeItemsByUserRespDto> content = items.getContent().stream().map(item -> {
            Trade trade = item.getTradeStatus() == TradeStatus.AVAILABLE ? null : tradeMap.get(item.getId());
            String imageUrl = imageMap.get(item.getId());
            String partnerNickname = getPartnerNickname(trade, item.getId());
            LocalDate completedDate = (trade != null && item.getTradeStatus() == TradeStatus.EXCHANGED)
                    ? trade.getModifiedAt().toLocalDate()
                    : null;
            GetExchangeItemsByUserRespDto.GetExchangeItemsByUserRespDtoBuilder builder = GetExchangeItemsByUserRespDto.builder()
                    .exchangeItemId(item.getId())
                    .exchangeItemName(item.getName())
                    .imageUrl(imageUrl)
                    .tradeStatus(item.getTradeStatus());

            if (item.getTradeStatus() == TradeStatus.AVAILABLE) {
                builder.size(item.getSize());
                builder.desiredItem(item.getDesiredItem());
            } else if (item.getTradeStatus() == TradeStatus.IN_EXCHANGE) {
                builder.size(item.getSize());
                builder.tradePartnerNickname(partnerNickname);
                builder.tradeId(trade != null ? trade.getId() : null);
            } else if (item.getTradeStatus() == TradeStatus.EXCHANGED) {
                builder.tradePartnerNickname(partnerNickname);
                builder.completedDate(trade != null ? completedDate : null);
                builder.tradeId(trade != null ? trade.getId() : null);
            }
            return builder.build();
        }).toList();
        return GetExchangeItemsAndPageByUserRespDto.builder()
                .content(content)
                .pageInfo(GetExchangeItemsAndPageByUserRespDto.PageInfo.builder()
                        .totalElements(items.getTotalElements())
                        .totalPages(items.getTotalPages())
                        .hasPrevious(items.hasPrevious())
                        .hasNext(items.hasNext())
                        .build())
                .build();
    }

    public GetExchangeItemRespDto getExchangeItemModifyPage(Long itemId, Long userId) {

        ExchangeItem exchangeItem = getValidExchangeItem(itemId);
        if (!exchangeItem.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        Category category = exchangeItem.getCategory();

        return GetExchangeItemRespDto.builder()
                .itemName(exchangeItem.getName())
                .description(exchangeItem.getDescription())
                .category(category)
                .itemQuality(exchangeItem.getItemQuality())
                .size(exchangeItem.getSize())
                .brand(exchangeItem.getBrand())
                .desiredItem(exchangeItem.getDesiredItem())
                .build();
    }

    public void validateDeposit(CreateExchangeItemReqDto reqDto, Long userId) {
        // 보증금이 0보다 작은 경우
        if (reqDto.getDeposit() < 0) {
            throw new BusinessException(ErrorCode.DEPOSIT_CANNOT_LESS_ZERO);
        }
        // 포인트가 없거나 포인트가 보증금보다 적은 경우
        Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_NOT_FOUND));
        if (point.getAmount() < reqDto.getDeposit()) {
            throw new BusinessException(ErrorCode.POINT_SHORTAGE);
        }
    }
    // 교환상대 닉네임 가져오기
    // trade 에서 해당 등록된 아이템들의 등록자id와 해당 유저의 id를 비교하여 상대방이 등록한 아이템을 통해 상대방의 닉네임을 추출
    private String getPartnerNickname(Trade trade, Long itemId) {
        if (trade == null) {
            return null;
        }
        ExchangeItem partnerItem = trade.getOwnerExchangeItem().getId().equals(itemId)
                ? trade.getRequesterExchangeItem()
                : trade.getOwnerExchangeItem();
        return partnerItem.getUser().getNickname();
    }
    // user 가져오기
    public User getValidUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user;
    }
    // category 가져오기
    public Category getValidCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        return category;
    }
    // Item 가져오기
    public ExchangeItem getValidExchangeItem(Long itemId) {
        ExchangeItem exchangeItem = exchangeItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));
        return exchangeItem;
    }
}
