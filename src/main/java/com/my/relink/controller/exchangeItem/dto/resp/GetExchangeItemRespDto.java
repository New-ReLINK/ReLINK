package com.my.relink.controller.exchangeItem.dto.resp;

import com.my.relink.domain.category.Category;
import com.my.relink.domain.item.donation.ItemQuality;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetExchangeItemRespDto {
    private String itemName;
    private String description;
    private Category category;
    private ItemQuality itemQuality;
    private String size;
    private String brand;
    private String desiredItem;
}
