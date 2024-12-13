package com.my.relink.controller.donation.dto.resp;

import com.my.relink.controller.donation.dto.PagingInfo;
import com.my.relink.domain.item.donation.DonationItem;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class DonationItemUserListRespDto {
    private List<DonationItemUserRespDto> items;
    private PagingInfo pagingInfo;

    public static DonationItemUserListRespDto of(Page<DonationItem> donationItems, PagingInfo pagingInfo) {
        List<DonationItemUserRespDto> dtoList = donationItems.getContent().stream()
                .map(DonationItemUserRespDto::fromEntity)
                .toList();
        return DonationItemUserListRespDto.builder()
                .items(dtoList)
                .pagingInfo(pagingInfo)
                .build();
    }
}

