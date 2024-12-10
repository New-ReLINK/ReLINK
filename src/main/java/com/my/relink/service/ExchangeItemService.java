package com.my.relink.service;

import com.my.relink.controller.exchangeItem.dto.req.CreateExchangeItemReqDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetExchangeItemRespDto;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.category.repository.CategoryRepository;
import com.my.relink.domain.image.EntityType;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<String, Object> getExchangeItemsByUserId(Long userId, int page, int size) {
        User user = getValidUser(userId);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ExchangeItem> items = exchangeItemRepository.findByUserId(user.getId(), pageable);
        // 교환 등록 물품이 없는 경우 빈 page 반환
        if (items.isEmpty()) {
            return Map.of(
                    "content", List.of(),
                    "pageInfo", Map.of(
                            "totalElements", 0,
                            "totalPages", 0,
                            "hasPrevious", false,
                            "hasNext", false
                    )
            );
        }
        List<GetExchangeItemRespDto> content = items.stream().map(item -> {
            Trade trade = tradeRepository.findByOwnerExchangeItemIdOrRequesterExchangeItemId(item.getId(), item.getId())
                    .orElse(null);
            String imageUrl = imageRepository.findFirstImageUrlByEntityTypeAndEntityIdOrderByCreatedAtAsc(EntityType.EXCHANGE_ITEM, item.getId())
                    .orElse(null);
            String partnerNickname = trade != null ? getPartnerNickname(trade, item) : null;
            LocalDate completedDate = (trade != null && item.getTradeStatus() == TradeStatus.EXCHANGED)
                    ? trade.getModifiedAt().toLocalDate()
                    : null;
            return GetExchangeItemRespDto.builder()
                    .exchangeItemId(item.getId())
                    .exchangeItemName(item.getName())
                    .imageUrl(imageUrl)
                    .tradeStatus(item.getTradeStatus())
                    .desiredItem(item.getDesiredItem())
                    .size(item.getSize())
                    .tradePartnerNickname(partnerNickname)
                    .tradeId(trade != null ? trade.getId() : null)
                    .completedDate(completedDate)
                    .build();
        }).toList();
        Map<String, Object> result = new HashMap<>();
        result.put("content", content);
        result.put("pageInfo", Map.of(
                "totalElements", items.getTotalElements(),
                "totalPages", items.getTotalPages(),
                "hasPrevious", items.hasPrevious(),
                "hasNext", items.hasNext()
        ));
        return result;
    }

    // 교환상대 닉네임 가져오기
    // trade 에서 해당 등록된 아이템들의 등록자id와 해당 유저의 id를 비교하여 상대방이 등록한 아이템을 통해 상대방의 닉네임을 추출
    private String getPartnerNickname(Trade trade, ExchangeItem currentItem) {
        Long partnerItemId = trade.getOwnerExchangeItem().getId().equals(currentItem.getId())
                ? trade.getRequesterExchangeItem().getId()
                : trade.getOwnerExchangeItem().getId();
        ExchangeItem partnerItem = exchangeItemRepository.findById(partnerItemId).orElse(null);
        return partnerItem != null ? partnerItem.getUser().getNickname() : null;
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
