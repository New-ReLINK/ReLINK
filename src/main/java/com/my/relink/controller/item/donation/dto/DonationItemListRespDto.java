package com.my.relink.controller.item.donation.dto;

import lombok.Builder;
import lombok.Getter;


import java.util.List;

@Getter
@Builder
public class DonationItemListRespDto {
    private long totalCompletedDonations; //총 완료된 기부 건수
    private long completedDonationsThisMonth; //이번달 완료된 기분 건수
    private List<DonationItemDto> items;
    private PagingInfo pagingInfo;
}
