package com.my.relink.service;

import com.my.relink.domain.category.Category;
import com.my.relink.domain.category.repository.CategoryRepository;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.controller.exchangeItem.dto.CreateExchangeItemReqDto;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.repository.PointRepository;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExchangeItemService {

    private final ExchangeItemRepository exchangeItemRepository;
    private final CategoryRepository categoryReopsitory;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;

    public long createExchangeItem(CreateExchangeItemReqDto reqDto, Long userId) {
        Category category = categoryReopsitory.findById(reqDto.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        // 보증금에 대한 유효성 검사
        validateDeposit(reqDto, userId);
        ExchangeItem exchangeItem = reqDto.toEntity(category, user);
        return exchangeItemRepository.save(exchangeItem).getId();
    }

    // 보증금에 대한 유효성 검사
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
}
