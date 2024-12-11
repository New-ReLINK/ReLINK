package com.my.relink.controller.item.donation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DonationItemDto {
    private Long id;
    private String name;
    private LocalDate completedDate;
}