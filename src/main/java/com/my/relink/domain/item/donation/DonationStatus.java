package com.my.relink.domain.item.donation;

import lombok.Getter;

@Getter
public enum DonationStatus {
    PENDING_REGISTRATION("접수 중"),
    REGISTRATION_COMPLETED("접수 완료"),
    UNDER_INSPECTION("검수 중"),
    INSPECTION_COMPLETED("검수 완료"),
    DONATION_COMPLETED("기부 완료"),
    INSPECTION_REJECTED("검수 부적합")
    ;
    
    private final String message;

    DonationStatus(String message) {
        this.message = message;
    }
}
