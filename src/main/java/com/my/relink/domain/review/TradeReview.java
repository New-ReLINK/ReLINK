package com.my.relink.domain.review;

import lombok.Getter;

@Getter
public enum TradeReview {

    TIME_PUNCTUAL("시간 약속을 잘 지켜요"),
    ITEM_AS_DESCRIBED("상품 상태가 설명과 같아요"),
    QUICK_RESPONSE("응답이 빨라요"),
    KIND_AND_MANNERED("친절하고 매너가 좋아요")

    ;

    private final String message;

    TradeReview(String message) {
        this.message = message;
    }
}
