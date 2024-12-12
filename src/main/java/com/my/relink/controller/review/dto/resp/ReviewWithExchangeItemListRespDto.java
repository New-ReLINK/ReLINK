package com.my.relink.controller.review.dto.resp;

import com.my.relink.domain.review.repository.dto.ReviewWithExchangeItemRepositoryDto;
import com.my.relink.util.DateTimeFormatterUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewWithExchangeItemListRespDto {
    private String itemName;
    private Double star;
    private String description;
    private String createAt;

    public ReviewWithExchangeItemListRespDto(ReviewWithExchangeItemRepositoryDto dto) {
        this.itemName = dto.getItemName();
        this.star = dto.getStar() != null ? dto.getStar().doubleValue() : 0.0;
        this.description = dto.getDescription();
        this.createAt = DateTimeFormatterUtil.format(dto.getCreateAt());
    }
}
