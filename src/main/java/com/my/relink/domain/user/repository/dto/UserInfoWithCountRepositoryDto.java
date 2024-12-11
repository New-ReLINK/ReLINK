package com.my.relink.domain.user.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoWithCountRepositoryDto {
    private long donationCount;
    private long exchangeCount;
    private String name;
    private String email;
    private Integer amount;
    private String profileUrl;
}
