package com.my.relink.controller.review.dto.resp;

import com.my.relink.domain.review.repository.dto.ReviewDetailWithExchangeItemRepositoryDto;
import com.my.relink.util.DateTimeFormatterUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetailWithExchangeItemListRespDto {
    private String exchangeItemName;
    private String nickName;
    private BigDecimal star;
    private String description;
    private String createAt;

    public ReviewDetailWithExchangeItemListRespDto(ReviewDetailWithExchangeItemRepositoryDto dto) {
        this.exchangeItemName = dto.getExchangeItemName();
        this.nickName = dto.getNickName();
        this.star = dto.getStar();
        this.description = dto.getDescription();
        this.createAt = DateTimeFormatterUtil.format(dto.getCreateAt());
    }
}
