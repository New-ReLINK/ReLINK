package com.my.relink.controller.exchangeItem.dto.resp;

import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.util.page.PageInfo;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class GetAllExchangeItemsRespDto {
    private List<GetAllExchangeItemsRespDto> content;
    private PageInfo pageInfo;
    private Long exchangeItemId;
    private String exchangeItemName;
    private TradeStatus tradeStatus;
    private ItemQuality itemQuality;
    private String desiredItem;
    private String imageUrl;
    private Long ownerId;
    private String ownerNickname;
    private int ownerTrustScore;

    public static GetAllExchangeItemsRespDto from(ExchangeItem item, Map<Long, String> imageMap, int trustScore) {
        return GetAllExchangeItemsRespDto.builder()
                .exchangeItemId(item.getId())
                .exchangeItemName(item.getName())
                .tradeStatus(item.getTradeStatus())
                .itemQuality(item.getItemQuality())
                .desiredItem(item.getDesiredItem())
                .imageUrl(imageMap.get(item.getId()))
                .ownerId(item.getUser().getId())
                .ownerNickname(item.getUser().getNickname())
                .ownerTrustScore(trustScore)
                .build();
    }

    public static GetAllExchangeItemsRespDto of(Page<GetAllExchangeItemsRespDto> contentPage) {
        return GetAllExchangeItemsRespDto.builder()
                .content(contentPage.getContent())
                .pageInfo(PageInfo.from(contentPage))
                .build();
    }
}
