package com.my.relink.controller.exchangeItem.dto.resp;

import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.util.page.PageInfo;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class GetAllExchangeItemsRespDto {
    private List<GetAllExchangeItemsRespDto> content;
    private PageInfo pageInfo;
    private Long exchangeItemId;
    private String exchangeItemName;
    private String tradeStatus;
    private ItemQuality itemQuality;
    private String desiredItem;
    private String imageUrl;
    private List<String> imageUrls;
    private Long ownerId;
    private String ownerNickname;
    private Integer ownerTrustScore;
    private Boolean like;
    private String description;
    private String category;
    private Integer deposit;
    private LocalDate createdAt;

    public static GetAllExchangeItemsRespDto fromAllItems(ExchangeItem item, Map<Long, String> imageMap, int trustScore) {
        return GetAllExchangeItemsRespDto.builder()
                .exchangeItemId(item.getId())
                .exchangeItemName(item.getName())
                .tradeStatus(item.getTradeStatus().getMessage())
                .itemQuality(item.getItemQuality())
                .desiredItem(item.getDesiredItem())
                .imageUrl(imageMap.get(item.getId()))
                .ownerId(item.getUser().getId())
                .ownerNickname(item.getUser().getNickname())
                .ownerTrustScore(trustScore)
                .build();
    }

    public static GetAllExchangeItemsRespDto fromItem(ExchangeItem item, List<String> imageUrls, int trustScore, boolean like) {
        return GetAllExchangeItemsRespDto.builder()
                .exchangeItemId(item.getId())
                .exchangeItemName(item.getName())
                .description(item.getDescription())
                .category(item.getCategory().getName())
                .deposit(item.getDeposit())
                .createdAt(item.getCreatedAt().toLocalDate())
                .tradeStatus(item.getTradeStatus().getMessage())
                .itemQuality(item.getItemQuality())
                .desiredItem(item.getDesiredItem())
                .imageUrls(imageUrls)
                .ownerId(item.getUser().getId())
                .ownerNickname(item.getUser().getNickname())
                .ownerTrustScore(trustScore)
                .like(like)
                .build();
    }

    public static GetAllExchangeItemsRespDto of(Page<GetAllExchangeItemsRespDto> contentPage) {
        return GetAllExchangeItemsRespDto.builder()
                .content(contentPage.getContent())
                .pageInfo(PageInfo.from(contentPage))
                .build();
    }
}
