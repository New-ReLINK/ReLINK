package com.my.relink.controller.donation.dto.resp;

import com.my.relink.domain.item.donation.DonationItem;
import com.my.relink.domain.item.donation.DonationStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DonationItemUserRespDto {
    private Long id;
    private String name;
    private String size;
    private DonationStatus donationStatus;
    private String destination;

    public static DonationItemUserRespDto fromEntity(DonationItem item) {
        if (item.getDonationStatus() == DonationStatus.DONATION_COMPLETED) {
            return DonationItemUserRespDto.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .size(item.getSize())
                    .donationStatus(item.getDonationStatus())
                    .destination(item.getDestination())
                    .build();
        }
        return DonationItemUserRespDto.builder()
                .id(item.getId())
                .name(item.getName())
                .size(item.getSize())
                .donationStatus(item.getDonationStatus())
                .build();
    }
}
