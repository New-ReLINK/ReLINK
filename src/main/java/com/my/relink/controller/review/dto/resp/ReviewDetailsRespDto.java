package com.my.relink.controller.review.dto.resp;

import com.my.relink.controller.exchange.dto.resp.ExchangeItemImageListRespDto;
import com.my.relink.domain.review.TradeReview;
import com.my.relink.domain.review.repository.dto.ReviewDetailRepositoryDto;
import com.my.relink.util.DateTimeFormatterUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetailsRespDto {
    private Long tradeId;
    private Long exchangeItemId;
    private String itemName;
    private String partnerNickname;
    private String description;
    private BigDecimal star;
    private String completedDate;
    List<TradeReview> tradeStatusList;
    List<ExchangeItemImageListRespDto> images;

    public ReviewDetailsRespDto(ReviewDetailRepositoryDto dto) {
        this.tradeId = dto.getTradeId();
        this.exchangeItemId = dto.getExchangeItemId();
        this.itemName = dto.getItemName();
        this.partnerNickname = dto.getPartnerNickname();
        this.description = dto.getDescription();
        this.star = dto.getStar();
        this.completedDate = DateTimeFormatterUtil.format(dto.getCompletedDate());
        this.tradeStatusList = dto.getTradeStatusList();
        this.images = dto.getImages();
    }
}
