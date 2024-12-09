package com.my.relink.controller.trade.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TradeResponse {

    private OwnerResponse ownerResponse;



    @NoArgsConstructor
    @Getter
    public static class OwnerResponse{
        private Long id;
        private String nickname;
        private Integer reliability; //신뢰도
    }
}
