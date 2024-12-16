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
    private List<DonationItemUserRespDto> underInspection;
    private List<DonationItemUserRespDto> inspectionComplete;
    private List<DonationItemUserRespDto> inspectionRejected;
    private List<DonationCompleteItemUserRespDto> donationComplete;
    private PagingInfo pagingInfo;

    public static DonationItemUserListRespDto of(Page<DonationItem> donationItems, PagingInfo pagingInfo) {
        List<DonationItemUserRespDto> underInspection = filterByStatus(donationItems, DonationStatus.UNDER_INSPECTION);
        List<DonationItemUserRespDto> inspectionComplete = filterByStatus(donationItems, DonationStatus.INSPECTION_COMPLETED);
        List<DonationItemUserRespDto> inspectionRejected = filterByStatus(donationItems, DonationStatus.INSPECTION_REJECTED);
        List<DonationCompleteItemUserRespDto> donationComplete = donationItems.getContent().stream()
                .filter(item -> item.getDonationStatus() == DonationStatus.DONATION_COMPLETED)
                .map(DonationCompleteItemUserRespDto::fromEntity)
                .collect(Collectors.toList());

        return DonationItemUserListRespDto.builder()
                .underInspection(underInspection)
                .inspectionComplete(inspectionComplete)
                .inspectionRejected(inspectionRejected)
                .donationComplete(donationComplete)
                .pagingInfo(pagingInfo)
                .build();
    }

    private static List<DonationItemUserRespDto> filterByStatus(Page<DonationItem> donationItems, DonationStatus status) {
        return donationItems.getContent().stream()
                .filter(item -> item.getDonationStatus() == status)
                .map(DonationItemUserRespDto::fromEntity)
                .collect(Collectors.toList());
    }

}