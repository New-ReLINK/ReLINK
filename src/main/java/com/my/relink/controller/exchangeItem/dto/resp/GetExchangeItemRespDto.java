package com.my.relink.controller.exchangeItem.dto.resp;

import com.my.relink.domain.category.Category;
import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.util.page.PageInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@ToString
public class GetExchangeItemRespDto {
    private List<GetExchangeItemRespDto> content;
    private PageInfo pageInfo;
    private Long exchangeItemId;
    private String exchangeItemName;
    private String imageUrl;
    private TradeStatus tradeStatus;
    private String desiredItem;
    private String size;
    private String tradePartnerNickname;
    private Long tradeId;
    private LocalDate completedDate;
    private String description;
    private Category category;
    private ItemQuality itemQuality;
    private String brand;
    private LocalDate createdAt;

    public static GetExchangeItemRespDto from(ExchangeItem item, Map<Long, Trade> tradeMap, Map<Long, String> imageMap) {
        Trade trade = item.getTradeStatus() == TradeStatus.AVAILABLE ? null : tradeMap.get(item.getId());
        String imageUrl = imageMap.get(item.getId());
        String partnerNickname = getPartnerNickname(trade, item.getId());
        LocalDate completedDate = (trade != null && item.getTradeStatus() == TradeStatus.EXCHANGED)
                ? trade.getModifiedAt().toLocalDate()
                : null;

        GetExchangeItemRespDtoBuilder builder = GetExchangeItemRespDto.builder()
                .exchangeItemId(item.getId())
                .exchangeItemName(item.getName())
                .imageUrl(imageUrl)
                .tradeStatus(item.getTradeStatus());

        if (item.getTradeStatus() == TradeStatus.AVAILABLE) {
            builder.size(item.getSize());
            builder.desiredItem(item.getDesiredItem());
        } else if (item.getTradeStatus() == TradeStatus.IN_EXCHANGE) {
            builder.size(item.getSize());
            builder.tradePartnerNickname(partnerNickname);
            builder.tradeId(trade != null ? trade.getId() : null);
        } else if (item.getTradeStatus() == TradeStatus.EXCHANGED) {
            builder.tradePartnerNickname(partnerNickname);
            builder.completedDate(trade != null ? completedDate : null);
            builder.tradeId(trade != null ? trade.getId() : null);
        }

        return builder.build();
    }

    // 교환상대 닉네임 가져오기
    // dto 생성에만 필요하기 때문에 같이 이동
    // trade 에서 해당 등록된 아이템들의 등록자id와 해당 유저의 id를 비교하여 상대방이 등록한 아이템을 통해 상대방의 닉네임을 추출
    private static String getPartnerNickname(Trade trade, Long itemId) {
        if (trade == null) {
            return null;
        }
        ExchangeItem partnerItem = trade.getOwnerExchangeItem().getId().equals(itemId)
                ? trade.getRequesterExchangeItem()
                : trade.getOwnerExchangeItem();
        return partnerItem.getUser().getNickname();
    }

    public static GetExchangeItemRespDto from(ExchangeItem exchangeItem) {
        return GetExchangeItemRespDto.builder()
                .exchangeItemId(exchangeItem.getId())
                .exchangeItemName(exchangeItem.getName())
                .description(exchangeItem.getDescription())
                .category(exchangeItem.getCategory())
                .itemQuality(exchangeItem.getItemQuality())
                .size(exchangeItem.getSize())
                .brand(exchangeItem.getBrand())
                .desiredItem(exchangeItem.getDesiredItem())
                .build();
    }
    public static GetExchangeItemRespDto from(ExchangeItem item, Map<Long, String> imageMap) {
        String imageUrl = imageMap.get(item.getId());
        return GetExchangeItemRespDto.builder()
                .exchangeItemId(item.getId())
                .exchangeItemName(item.getName())
                .size(item.getSize())
                .imageUrl(imageUrl)
                .desiredItem(item.getDesiredItem())
                .createdAt(item.getCreatedAt().toLocalDate())
                .build();
    }

    public static GetExchangeItemRespDto empty(Pageable pageable) {
        return GetExchangeItemRespDto.builder()
                .content(List.of())
                .pageInfo(PageInfo.builder()
                        .totalCount(0)
                        .totalPages(0)
                        .hasPrevious(pageable.getPageNumber() > 0)
                        .hasNext(false)
                        .build())
                .build();
    }

    public static GetExchangeItemRespDto of(Page<GetExchangeItemRespDto> itemsPage) {
        return GetExchangeItemRespDto.builder()
                .content(itemsPage.getContent())
                .pageInfo(PageInfo.from(itemsPage))
                .build();
    }
}
