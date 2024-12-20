package com.my.relink.controller.donation.dto.resp;

import com.my.relink.domain.item.donation.DonationItem;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DonationCompleteItemDetailRespDto {
    private Long id;
    private String name;
    private String description;
    private String destination;
    private String imageUrl;
    private LocalDate donationDate;
    private String certificateUrl; //기부 인증서

    public static DonationCompleteItemDetailRespDto fromEntity(DonationItem item, String imageUrl, String certificateUrl) {
        return DonationCompleteItemDetailRespDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .destination(item.getDestination())
                .imageUrl(imageUrl)
                .donationDate(item.getModifiedAt().toLocalDate())
                .certificateUrl(certificateUrl)
                .build();
    }
}
