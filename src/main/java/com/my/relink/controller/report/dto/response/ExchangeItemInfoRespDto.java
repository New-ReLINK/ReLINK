package com.my.relink.controller.report.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ExchangeItemInfoRespDto {

    private String ownerNickname;
    private String exchangeItemImageUrl;
    private String exchangeItemName;
    private Long exchangeItemId;
}
