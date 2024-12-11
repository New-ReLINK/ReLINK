package com.my.relink.controller.item.donation.dto.resp;

import com.my.relink.controller.item.donation.dto.PagingInfo;
import com.my.relink.domain.item.donation.DonationItem;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;


import java.util.List;

@Getter
@Builder
public class DonationItemListRespDto {
    private long totalCompletedDonations; //총 완료된 기부 건수
    private long completedDonationsThisMonth; //이번달 완료된 기분 건수
    private List<DonationItemRespDto> items;
    private PagingInfo pagingInfo;

    public static DonationItemListRespDto of(
            Page<DonationItem> donationItems,
            long totalCompletedDonations,
            long completedDonationsThisMonth) {

        return DonationItemListRespDto.builder()
                .totalCompletedDonations(totalCompletedDonations)
                .completedDonationsThisMonth(completedDonationsThisMonth)
                .items(donationItems.getContent().stream()
                        .map(DonationItemRespDto::fromEntity)
                        .toList())
                .pagingInfo(PagingInfo.fromPage(donationItems))
                .build();
    }
}
