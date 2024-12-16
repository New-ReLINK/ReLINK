package com.my.relink.controller.exchangeItem.dto.resp;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Builder
@Getter
public class GetExchangeItemsRespDto {
    private List<GetExchangeItemRespDto> content;
    private PageInfo pageInfo;

    public static GetExchangeItemsRespDto empty(Pageable pageable) {
        return GetExchangeItemsRespDto.builder()
                .content(List.of())
                .pageInfo(PageInfo.builder()
                        .totalElements(0)
                        .totalPages(0)
                        .hasPrevious(pageable.getPageNumber() > 0)
                        .hasNext(false)
                        .build())
                .build();
    }

    public static GetExchangeItemsRespDto of(Page<GetExchangeItemRespDto> page) {
        return GetExchangeItemsRespDto.builder()
                .content(page.getContent())
                .pageInfo(PageInfo.builder()
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .hasPrevious(page.hasPrevious())
                        .hasNext(page.hasNext())
                        .build())
                .build();
    }

    @Getter
    @Builder
    public static class PageInfo {
        private long totalElements;
        private int totalPages;
        private boolean hasPrevious;
        private boolean hasNext;
    }
}
