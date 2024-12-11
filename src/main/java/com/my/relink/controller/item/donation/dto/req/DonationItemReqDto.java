package com.my.relink.controller.item.donation.dto.req;

import com.my.relink.controller.item.donation.validation.ValidItemQuality;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.item.donation.DonationItem;
import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.user.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DonationItemReqDto {

    @NotBlank(message = "기부하실 상품의 이름을 작성해 주세요.")
    @Size(max = 30, message = "상품 명은 최대 20자까지 입니다.")
    private String name; // 기부 상품 명

    @NotNull(message = "카테고리를 선택해 주세요.")
    private Long categoryId; // 카테고리 ID

    @NotNull(message = "상품의 상태를 선택해 주세요.")
    @ValidItemQuality
    private String itemQuality; // 상품 상태 (Enum: NEW, USED, DEFECTIVE)

    private String description; // 상세 설명 (nullable)

    @Size(max = 128, message = "희망 기부처는 최대 128자까지 입니다.")
    private String desiredDestination; // 희망 기부처 (nullable)

    public DonationItem toEntity(User user, Category category) {
        return new DonationItem(
                name,
                description,
                desiredDestination,
                user,
                category,
                ItemQuality.valueOf(itemQuality) // Enum 변환
        );
    }
}
