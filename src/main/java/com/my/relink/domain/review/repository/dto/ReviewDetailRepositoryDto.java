package com.my.relink.domain.review.repository.dto;

import com.my.relink.controller.exchange.dto.resp.ExchangeItemImageListRespDto;
import com.my.relink.domain.review.TradeReview;
import com.my.relink.util.DateTimeFormatterUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetailRepositoryDto {

    private Long tradeId;
    private Long exchangeItemId;
    private String itemName;
    private String partnerNickname;
    private String description;
    private BigDecimal star;
    private LocalDateTime completedDate;
    List<TradeReview> tradeStatusList;
    List<ExchangeItemImageListRespDto> images;

    public ReviewDetailRepositoryDto(ReviewDetailWithOutTradeReview dto, List<TradeReview> tradeStatusList, List<ExchangeItemImageListRespDto> images) {
        this.tradeId = dto.getTradeId();
        this.exchangeItemId = dto.getExchangeItemId();
        this.itemName = dto.getItemName();
        this.partnerNickname = dto.getPartnerNickname();
        this.description = dto.getDescription();
        this.star = dto.getStar();
        this.completedDate = dto.getCompletedDate();
        this.tradeStatusList = tradeStatusList;
        this.images = images;
    }
}
