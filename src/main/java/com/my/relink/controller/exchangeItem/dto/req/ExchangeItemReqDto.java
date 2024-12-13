package com.my.relink.controller.exchangeItem.dto.req;

import com.my.relink.common.validation.EnumValidator;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
@Builder
@AllArgsConstructor
public class ExchangeItemReqDto {
    @NotBlank(message = "상품명을 입력해주세요.")
    @Length(max = 30, message = "상품명은 30자 이내로 입력해주세요.")
    private String name;
    @NotBlank(message = "상품 상세 설명을 입력해주세요.")
    private String description;
    @NotNull(message = "카테고리를 선택해주세요.")
    private Long categoryId;
    @NotNull(message = "상품의 상태를 선택해주세요")
    @EnumValidator(enumClass = ItemQuality.class, message = "유효하지 않은 상품 상태입니다.")
    private ItemQuality itemQuality;
    @Length(max = 20, message = "사이즈는 20자 이내로 입력해주세요.")
    private String size;
    @Length(max = 50, message = "브랜드는 50자 이내로 입력해주세요.")
    private String brand;
    @Length(max = 200, message = "교환 희망 상품은 200자 이내로 입력해주세요.")
    private String desiredItem;
    @NotNull(message = "교환시 원하는 보증금액을 입력해주세요.")
    private Integer deposit;
    private Boolean isDeleted;

    public ExchangeItem toEntity(Category category, User user) {
        return ExchangeItem.builder()
                .name(this.name)
                .description(this.description)
                .category(category)
                .user(user)
                .itemQuality(this.itemQuality)
                .size(this.size)
                .brand(this.brand)
                .desiredItem(this.desiredItem)
                .deposit(this.deposit)
                .isDeleted(false)
                .tradeStatus(TradeStatus.AVAILABLE)
                .build();
    }
}
