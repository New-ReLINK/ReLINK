package com.my.relink.controller.item.donation.dto.resp;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DonationItemRespDto {
    private Long id;
    private String name;
    private LocalDate completedDate;
}