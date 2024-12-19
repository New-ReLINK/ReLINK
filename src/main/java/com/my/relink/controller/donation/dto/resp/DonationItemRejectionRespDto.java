package com.my.relink.controller.donation.dto.resp;

import com.my.relink.domain.item.donation.DonationItem;
import com.my.relink.domain.item.donation.DonationStatus;
import com.my.relink.domain.item.donation.RejectedReason;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Builder
public class DonationItemRejectionRespDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private String category;
    private String donationStatus;
    private LocalDate modifiedAt;
    private String rejectedReason;
    private String detailReiectedReason;

    public static DonationItemRejectionRespDto fromEntity(DonationItem item, String imageUrl, RejectedReason rejectedReason) {
        return DonationItemRejectionRespDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .category(item.getCategory().getName())
                .imageUrl(imageUrl)
                .donationStatus(item.getDonationStatus().getMessage())
                .modifiedAt(item.getModifiedAt().toLocalDate())
                .rejectedReason(item.getRejectedReason().getMessage())
                .detailReiectedReason(item.getDetailRejectedReason())
                .build();
    }
}