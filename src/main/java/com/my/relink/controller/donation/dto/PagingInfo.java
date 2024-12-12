package com.my.relink.controller.donation.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
public class PagingInfo {
    private long totalDataCount;
    private int totalPages;
    private boolean hasPrevious;
    private boolean hasNext;

    public static PagingInfo fromPage(Page<?> page) {
        return PagingInfo.builder()
                .totalDataCount(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasPrevious(page.hasPrevious())
                .hasNext(page.hasNext())
                .build();
    }
}
