package com.my.relink.service;

import com.my.relink.controller.exchangeItem.dto.req.CreateExchangeItemReqDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetExchangeItemRespDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetExchangeItemsByUserRespDto;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.category.repository.CategoryRepository;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.ImageRepository;
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
        Category category = getValidCategory(reqDto);
        User user = getValidUser(userId);
        // 보증금에 대한 유효성 검사
        validateDeposit(reqDto, userId);
        ExchangeItem exchangeItem = reqDto.toEntity(category, user);
        return exchangeItemRepository.save(exchangeItem).getId();
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

    public GetExchangeItemsByUserRespDto getExchangeItemsByUserId(Long userId, int page, int size) {
        User user = getValidUser(userId);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ExchangeItem> items = exchangeItemRepository.findByUserId(user.getId(), pageable);
        if (items.isEmpty()) {
            return GetExchangeItemsByUserRespDto.builder()
                    .content(List.of())
                    .pageInfo(GetExchangeItemsByUserRespDto.PageInfo.builder()
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
                .collect(Collectors.toMap(
                        trade -> trade.getOwnerExchangeItem().getId().equals(userId)
                                ? trade.getRequesterExchangeItem().getId()
                                : trade.getOwnerExchangeItem().getId(),
                        trade -> trade
                ));
        List<Image> images = imageRepository.findFirstImagesByEntityTypeAndEntityIds(EntityType.EXCHANGE_ITEM, itemIds);
        Map<Long, String> imageMap = images.stream()
                .collect(Collectors.toMap(Image::getEntityId, Image::getImageUrl));
        List<GetExchangeItemRespDto> content = items.getContent().stream().map(item -> {
            Trade trade = tradeMap.get(item.getId());
            String imageUrl = imageMap.get(item.getId());
            String partnerNickname = getPartnerNickname(trade, item);
            LocalDate completedDate = (trade != null && item.getTradeStatus() == TradeStatus.EXCHANGED)
                    ? trade.getModifiedAt().toLocalDate()
                    : null;
            return GetExchangeItemRespDto.builder()
                    .exchangeItemId(item.getId())
                    .exchangeItemName(item.getName())
                    .imageUrl(imageUrl)
                    .tradeStatus(item.getTradeStatus())
                    .desiredItem(item.getTradeStatus() == TradeStatus.AVAILABLE ? item.getDesiredItem() : null)
                    .size(item.getTradeStatus() == TradeStatus.AVAILABLE || item.getTradeStatus() == TradeStatus.IN_EXCHANGE ? item.getSize() : null)
                    .tradePartnerNickname(partnerNickname)
                    .tradeId(item.getTradeStatus() == TradeStatus.IN_EXCHANGE || item.getTradeStatus() == TradeStatus.EXCHANGED ? trade.getId() : null)
                    .completedDate(item.getTradeStatus() == TradeStatus.EXCHANGED ? completedDate : null)
                    .build();
        }).toList();
        return GetExchangeItemsByUserRespDto.builder()
                .content(content)
                .pageInfo(GetExchangeItemsByUserRespDto.PageInfo.builder()
                        .totalElements(items.getTotalElements())
                        .totalPages(items.getTotalPages())
                        .hasPrevious(items.hasPrevious())
                        .hasNext(items.hasNext())
                        .build())
                .build();
    }

    // 교환상대 닉네임 가져오기
    // trade 에서 해당 등록된 아이템들의 등록자id와 해당 유저의 id를 비교하여 상대방이 등록한 아이템을 통해 상대방의 닉네임을 추출
    private String getPartnerNickname(Trade trade, ExchangeItem currentItem) {
        ExchangeItem partnerItem = trade.getOwnerExchangeItem().getId().equals(currentItem.getId())
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
    public Category getValidCategory(CreateExchangeItemReqDto reqDto) {
        Category category = categoryRepository.findById(reqDto.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        return category;
    }
}
