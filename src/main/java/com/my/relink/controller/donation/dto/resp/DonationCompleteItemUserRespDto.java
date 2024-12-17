package com.my.relink.controller.donation.dto.resp;

import com.my.relink.domain.item.donation.DonationItem;
import com.my.relink.domain.item.donation.DonationStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DonationCompleteItemUserRespDto {
    private Long id;
    private String name;
    private String size;
    private DonationStatus donationStatus;
    private String destination;
    private String statusMessage;

    public static DonationCompleteItemUserRespDto fromEntity(DonationItem item) {
        return DonationCompleteItemUserRespDto.builder()
                .id(item.getId())
                .name(item.getName())
                .size(item.getSize())
                .donationStatus(item.getDonationStatus())
                .destination(item.getDestination())
                .statusMessage(getStatusMessage(item.getDonationStatus()))
                .build();
    }

    private static String getStatusMessage(DonationStatus status) {
        return switch (status) {
            case PENDING_REGISTRATION -> "접수중";
            case REGISTRATION_COMPLETED -> "접수완료";
            case UNDER_INSPECTION -> "검수 중";
            case INSPECTION_COMPLETED -> "검수 완료";
            case INSPECTION_REJECTED -> "검수 부적합";
            case DONATION_COMPLETED -> "기부 완료";
        };
    }

}
