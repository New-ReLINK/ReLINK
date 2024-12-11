package com.my.relink.controller.user.dto.resp;

import com.my.relink.domain.user.repository.dto.UserInfoWithCountRepositoryDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRespDto {

    private long donationCount;
    private long exchangeCount;
    private String name;
    private String email;
    private Integer amount;
    private String profileUrl;
    private int trustScore;

    public UserProfileRespDto(Double avgStar, UserInfoWithCountRepositoryDto dto) {
        this.donationCount = dto.getDonationCount();
        this.exchangeCount = dto.getExchangeCount();
        this.name = dto.getName();
        this.email = dto.getEmail();
        this.amount = dto.getAmount();
        this.profileUrl = dto.getProfileUrl();
        this.trustScore = avgStar != null ? (int) (avgStar * 20) : 0;
    }
}
