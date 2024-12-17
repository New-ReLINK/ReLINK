package com.my.relink.controller.trade.dto.response;

import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.Address;
import com.my.relink.domain.user.User;
import com.my.relink.util.DateTimeUtil;
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

    // 정적 팩토리 메서드
    public static TradeCompletionRespDto from(ExchangeItem myExchangeItem, ExchangeItem partnerExchangeItem,
                                              String myImage, String partnerImage, User partnerUser,
                                              Trade trade, DateTimeUtil dateTimeUtil) {

        // 교환 상품 정보
        TradeItemInfo myItem = TradeItemInfo.builder()
                .itemName(myExchangeItem.getName())
                .itemQuality(myExchangeItem.getItemQuality())
                .itemId(myExchangeItem.getId())
                .itemImageUrl(myImage)
                .build();

        TradeItemInfo partnerItem = TradeItemInfo.builder()
                .itemName(partnerExchangeItem.getName())
                .itemQuality(partnerExchangeItem.getItemQuality())
                .itemId(partnerExchangeItem.getId())
                .itemImageUrl(partnerImage)
                .build();

        // 유저 정보
        UserInfo partnerInfo = UserInfo.builder()
                .partnerAddress(trade.isRequester(partnerUser.getId()) ? trade.getOwnerAddress() : trade.getRequesterAddress())
                .build();

        // 진행 상태 정보
        String completedAt = dateTimeUtil.getTradeStatusFormattedTime(trade.getModifiedAt());
        TradeStatusInfo tradeStatusInfo = TradeStatusInfo.builder()
                .completedAt(completedAt)
                .tradeStatus(trade.getTradeStatus())
                .build();

        // DTO 반환
        return new TradeCompletionRespDto(myItem, partnerItem, partnerInfo, tradeStatusInfo);
    }
}
