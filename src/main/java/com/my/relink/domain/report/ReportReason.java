package com.my.relink.domain.report;

import lombok.Getter;

@Getter
public enum ReportReason {

    WRONG_ITEM_DELIVERY("다른 물품 배송"),
    FALSE_INFORMATION("허위 정보 기재"),
    ILLEGAL_ITEM("불법 아이템"),
    NO_RESPONSE("응답 없음"),
    PROMISE_VIOLATION("약속 불이행"),
    OTHER("기타"),
    MISLEADING_DESCRIPTION("상품 설명 불일치"),
    COMMERCIAL_USE("상업적 용도"),
    PERSONAL_INFO("개인 정보 노출"),
    HARASSMENT("혐오/비방/비하 표현"),
    SPAM("도배성/스팸 게시물")

    ;

    private final String message;

    ReportReason(String message) {
        this.message = message;
    }
}
