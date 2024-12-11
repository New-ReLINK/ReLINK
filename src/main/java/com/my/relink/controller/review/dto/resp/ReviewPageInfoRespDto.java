package com.my.relink.controller.review.dto.resp;

import com.my.relink.util.page.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewPageInfoRespDto {
    private long totalReviewCount;
    private PageResponse<ReviewListRespDto> page;
}
