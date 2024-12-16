package com.my.relink.controller.donation.dto.resp;

import com.my.relink.controller.donation.dto.PagingInfo;
import com.my.relink.domain.item.donation.DonationItem;
import com.my.relink.domain.item.donation.DonationStatus;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class DonationItemUserListRespDto {
    private List<DonationItemUserRespDto> inInspection;
    private List<DonationItemUserRespDto> inspected;
    private List<DonationItemUserRespDto> unsuitable;
    private List<DonationItemUserRespDto> donationCompleted;
    private PagingInfo pagingInfo;

    public static DonationItemUserListRespDto of(Page<DonationItem> donationItems, PagingInfo pagingInfo) {
        List<DonationItemUserRespDto> allItems = donationItems.getContent().stream()
                .map(DonationItemUserRespDto::fromEntity)
                .toList();

        return DonationItemUserListRespDto.builder()
                .inInspection(filterByStatus(allItems, DonationStatus.UNDER_INSPECTION))
                .inspected(filterByStatus(allItems, DonationStatus.INSPECTION_COMPLETED))
                .unsuitable(filterByStatus(allItems, DonationStatus.INSPECTION_REJECTED))
                .donationCompleted(filterByStatus(allItems, DonationStatus.DONATION_COMPLETED))
                .pagingInfo(pagingInfo)
                .build();
    }

    private static List<DonationItemUserRespDto> filterByStatus(List<DonationItemUserRespDto> items, DonationStatus status) {
        return items.stream()
                .filter(item -> item.getDonationStatus() == status)
                .collect(Collectors.toList());
    }
}