package com.my.relink.controller.trade.dto.response;

import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TradeCompletionRespDto {

    // 교환 상품 정보
    @Getter
    @Builder
    @AllArgsConstructor
    public static class TradeItemInfo {
        private String itemName;
        private ItemQuality itemQuality;
        private Long itemId;
        private String itemImageUrl;
    }

    private TradeItemInfo myItem;
    private TradeItemInfo partnerItem;

    // 유저 정보
    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserInfo {
        private Address partnerAddress;
    }

    private UserInfo partnerInfo;

    // 진행 상황 정보
    @Getter
    @Builder
    @AllArgsConstructor
    public static class TradeStatusInfo {
        private String completedAt;   // 교환 완료일 (형식: "2024년 3월 21일 15:30")
        private TradeStatus tradeStatus;   // 교환 상태 (예: "교환 완료")
    }

    private TradeStatusInfo tradeStatusInfo;
}
