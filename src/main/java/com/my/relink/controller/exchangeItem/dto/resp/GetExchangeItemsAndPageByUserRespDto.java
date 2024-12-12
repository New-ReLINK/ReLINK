package com.my.relink.controller.exchangeItem.dto.resp;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Builder
@Getter
public class GetExchangeItemsAndPageByUserRespDto {
    private List<GetExchangeItemsByUserRespDto> content;
    private PageInfo pageInfo;

    @Data
    @Builder
    public static class PageInfo {
        private long totalElements;
        private int totalPages;
        private boolean hasPrevious;
        private boolean hasNext;
    }
}
