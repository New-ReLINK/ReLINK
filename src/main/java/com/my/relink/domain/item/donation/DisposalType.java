package com.my.relink.domain.item.donation;

import lombok.Getter;

@Getter
public enum DisposalType {

    RETURNED("반송"),
    DISCARD("폐기");

    private final String message;

    DisposalType(String message) {
        this.message = message;
    }
}
