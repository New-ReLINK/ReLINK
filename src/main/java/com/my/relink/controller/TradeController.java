package com.my.relink.controller;

import com.my.relink.domain.trade.dto.TradeRequestDto;
import com.my.relink.domain.trade.dto.TradeRequestResponseDto;
import com.my.relink.service.TradeService;
import com.my.relink.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TradeController {
    private final TradeService tradeService;

    @PostMapping("/trades/{tradeId}/request")
    public ResponseEntity<ApiResult<Long>> requestTrade(@PathVariable(name = "tradeId") Long tradeId, @RequestBody TradeRequestDto tradeRequestDto) {//추후 로그인 유저로 바뀔 얘정
        tradeService.requestTrade(tradeId, tradeRequestDto.getUserId());
        return ResponseEntity.ok(ApiResult.success(tradeId));
    }
}
