package com.my.relink.controller.item.donation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DonationItemReqDto {

    @NotBlank
    @Size(max = 30)
    private String name; // 기부 상품 명

    @NotNull
    private Long categoryId; // 카테고리 ID

    @NotNull
    private String itemQuality; // 상품 상태 (Enum: NEW, USED, DEFECTIVE)
    private String description; // 상세 설명 (nullable)

    @Size(max = 128)
    private String desiredDestination; // 희망 기부처 (nullable)
}
