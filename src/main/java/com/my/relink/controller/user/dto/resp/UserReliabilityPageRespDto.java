package com.my.relink.controller.user.dto.resp;

import com.my.relink.controller.review.dto.resp.ReviewDetailWithExchangeItemListRespDto;
import com.my.relink.util.page.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserReliabilityPageRespDto {
    private int trustScore;
    private Double avgStar;
    private long totalExchangeItem;
    private long totalDonationItem;

    PageResponse<ReviewDetailWithExchangeItemListRespDto> reviews;

    public UserReliabilityPageRespDto(
            long totalExchangeItem,
            long totalDonationItem,
            Double avgStar,
            Page<ReviewDetailWithExchangeItemListRespDto> page
    ) {
        this.trustScore = avgStar != null ? (int) (avgStar * 20) : 0;
        this.avgStar = avgStar != null ? avgStar : 0.0;
        this.totalExchangeItem = totalExchangeItem;
        this.totalDonationItem = totalDonationItem;
        this.reviews = PageResponse.of(page);
    }
}
