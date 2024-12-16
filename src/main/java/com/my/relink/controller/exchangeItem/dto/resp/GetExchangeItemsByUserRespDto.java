package com.my.relink.controller.exchangeItem.dto.resp;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Builder
@Getter
public class GetExchangeItemsByUserRespDto {
    private List<GetExchangeItemRespDto> content;
    private PageInfo pageInfo;

    public static GetExchangeItemsByUserRespDto empty(Pageable pageable) {
        return GetExchangeItemsByUserRespDto.builder()
                .content(List.of())
                .pageInfo(PageInfo.builder()
                        .totalElements(0)
                        .totalPages(0)
                        .hasPrevious(pageable.getPageNumber() > 0)
                        .hasNext(false)
                        .build())
                .build();
    }

    public static GetExchangeItemsByUserRespDto of(Page<GetExchangeItemRespDto> page) {
        return GetExchangeItemsByUserRespDto.builder()
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
