package com.my.relink.util.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
@AllArgsConstructor
public class PageInfo {

    private int totalPages;
    private long totalCount;
    private boolean hasPrevious;
    private boolean hasNext;

    public static PageInfo from(Page<?> page) {
        return new PageInfo(
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasPrevious(),
                page.hasNext()
        );
    }
}
