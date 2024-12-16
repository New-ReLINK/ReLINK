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
    private List<DonationItemUserRespDto> under_inspection;
    private List<DonationItemUserRespDto> inspection_complete;
    private List<DonationItemUserRespDto> inspection_rejected;
    private List<DonationItemUserRespDto> donation_complete;
    private PagingInfo pagingInfo;

    public static DonationItemUserListRespDto of(Page<DonationItem> donationItems, PagingInfo pagingInfo) {
        List<DonationItemUserRespDto> allItems = donationItems.getContent().stream()
                .map(DonationItemUserRespDto::fromEntity)
                .toList();

        return DonationItemUserListRespDto.builder()
                .under_inspection(filterByStatus(allItems, DonationStatus.UNDER_INSPECTION))
                .inspection_complete(filterByStatus(allItems, DonationStatus.INSPECTION_COMPLETED))
                .inspection_rejected(filterByStatus(allItems, DonationStatus.INSPECTION_REJECTED))
                .donation_complete(filterByStatus(allItems, DonationStatus.DONATION_COMPLETED))
                .pagingInfo(pagingInfo)
                .build();
    }

    private static List<DonationItemUserRespDto> filterByStatus(List<DonationItemUserRespDto> items, DonationStatus status) {
        return items.stream()
                .filter(item -> item.getDonationStatus() == status)
                .collect(Collectors.toList());
    }
}