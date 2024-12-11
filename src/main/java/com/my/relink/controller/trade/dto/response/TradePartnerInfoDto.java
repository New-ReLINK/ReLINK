package com.my.relink.controller.trade.dto.response;

import com.my.relink.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TradePartnerInfoDto {
    private String nickname;
    private Long userId;
    private int trustScore; //신뢰도

    public TradePartnerInfoDto(User partner, int trustScore) {
        this.nickname = partner.getNickname();
        this.userId = partner.getId();
        this.trustScore = trustScore;
    }
}
