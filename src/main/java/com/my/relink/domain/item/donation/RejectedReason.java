package com.my.relink.domain.item.donation;

import lombok.Getter;

@Getter
public enum RejectedReason {
    QUALITY_STANDARD_FAILED("품질 기준 미달"),
    DAMAGED("물품 파손/훼손"),
    RESTRICTED("제한된 물품"),
    SAFETY_HAZARD("안전상 위험 물품"),
    INCOMPLETE("구성품 누락"),
    DIMENSION_LIMIT_EXCEEDED("크기/무게 제한 초과")

    ;

    private final String message;

    RejectedReason(String message) {
        this.message = message;
    }
}
