package com.my.relink.controller.donation.dto.resp;

import com.my.relink.domain.item.donation.DonationItem;
import com.my.relink.domain.item.donation.DonationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Builder
public class DonationItemDetailRespDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private String category;
    private DonationStatus donationStatus;
    private LocalDate createdAt;

    public static DonationItemDetailRespDto fromEntity(DonationItem item,  Map<Long, String> imageMap) {
        String imageUrl = imageMap.get(item.getId());

        return DonationItemDetailRespDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .category(item.getCategory().getName())
                .imageUrl(imageUrl)
                .donationStatus(item.getDonationStatus())
                .createdAt(item.getCreatedAt().toLocalDate())
                .build();
    }
}



