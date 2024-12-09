package com.my.relink.service;

import com.my.relink.domain.category.Category;
import com.my.relink.domain.category.repository.CategoryRepository;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.dto.CreateExchangeItemReqDto;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.trade.TradeStatus;
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


    public long createExchangeItem(CreateExchangeItemReqDto reqDto) {

        Category category = categoryReopsitory.findById(reqDto.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        User user = userRepository.findById(reqDto.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 보증금이 0보다 작은 경우
        if (reqDto.getDeposit() <= 0) {
            throw new BusinessException(ErrorCode.DEPOSIT_CANNOT_LESS_ZERO);
        }

        ExchangeItem exchangeItem = ExchangeItem.builder()
                .name(reqDto.getName())
                .description(reqDto.getDescription())
                .category(category)
                .user(user)
                .itemQuality(reqDto.getItemQuality())
                .deposit(reqDto.getDeposit())
                .size(reqDto.getSize())
                .brand(reqDto.getBrand())
                .desiredItem(reqDto.getDesiredItem())
                .tradeStatus(TradeStatus.AVAILABLE) // 초기값 AVAILABLE
                .build();
        return exchangeItemRepository.save(exchangeItem).getId();
    }
}
