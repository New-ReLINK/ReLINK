package com.my.relink.domain.item.exchange.dto;

import com.my.relink.domain.item.donation.ItemQuality;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
@Builder
public class CreateExchangeItemReqDto {
    @NotBlank(message = "상품명을 입력해주세요.")
    @Length(max = 30, message = "상품명은 30자 이내로 입력해주세요.")
    private String name;
    @NotBlank(message = "상품 상세 설명을 입력해주세요.")
    private String description;
    @NotNull(message = "카테고리를 선택해주세요.")
    private Long categoryId;
    @NotNull(message = "상품의 상태를 선택해주세요")
    private ItemQuality itemQuality;
    @Length(max = 20, message = "사이즈는 20자 이내로 입력해주세요.")
    private String size;
    @Length(max = 50, message = "브랜드는 50자 이내로 입력해주세요.")
    private String brand;
    @Length(max = 200, message = "교환 희망 상품은 200자 이내로 입력해주세요.")
    private String desiredItem;
    @NotNull(message = "교환시 원하는 보증금액을 입력해주세요.")
    private Integer deposit;

    // 임시로 사용자 정보 dto로 요청 - 추후 토큰에서 email을 받아 id를 찾기
    @NotNull(message = "사용자 ID를 입력해주세요.")
    private Long userId;
}
