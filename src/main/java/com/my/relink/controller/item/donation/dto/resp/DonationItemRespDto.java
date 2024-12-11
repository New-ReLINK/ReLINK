package com.my.relink.controller.item.donation.dto.resp;

import com.my.relink.controller.item.donation.dto.PagingInfo;
import com.my.relink.domain.item.donation.DonationItem;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

@Getter
@Builder
public class DonationItemRespDto {
    private Long id;
    private String name;
    private LocalDate completedDate;

    public static DonationItemRespDto fromEntity(DonationItem item) {
        return DonationItemRespDto.builder()
                .id(item.getId())
                .name(item.getName())
                .completedDate(item.getModifiedAt().toLocalDate())
                .build();
    }
}