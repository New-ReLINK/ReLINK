package com.my.relink.service;

import com.my.relink.controller.exchangeItem.dto.req.CreateExchangeItemReqDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetExchangeItemRespDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetExchangeItemsByUserRespDto;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.category.repository.CategoryRepository;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.repository.PointRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExchangeItemService {

    private final ExchangeItemRepository exchangeItemRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final TradeService tradeService;
    private final ImageService imageService;

    public long createExchangeItem(CreateExchangeItemReqDto reqDto, Long userId) {
        Category category = getValidCategory(reqDto.getCategoryId());
        User user = getValidUser(userId);
        // 보증금에 대한 유효성 검사
        validateDeposit(reqDto.getDeposit(), userId);
        ExchangeItem exchangeItem = reqDto.toEntity(category, user);
        return exchangeItemRepository.save(exchangeItem).getId();
    }

    public GetExchangeItemsByUserRespDto getExchangeItemsByUserId(Long userId, int page, int size) {
        User user = getValidUser(userId);
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<ExchangeItem> items = exchangeItemRepository.findByUserId(user.getId(), pageable);
        if (items.isEmpty()) {
            return GetExchangeItemsByUserRespDto.empty(pageable);
        }

        List<Long> itemIds = items.getContent().stream().map(ExchangeItem::getId).toList();
        Map<Long, Trade> tradeMap = tradeService.getTradesByItemIds(itemIds);
        Map<Long, String> imageMap = imageService.getImagesByItemIds(EntityType.EXCHANGE_ITEM, itemIds);

        Page<GetExchangeItemRespDto> content = items.map(item -> GetExchangeItemRespDto.from(item, tradeMap, imageMap));

        return GetExchangeItemsByUserRespDto.of(content);
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
