package com.my.relink.controller.trade;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.trade.dto.response.TradeInquiryDetailRespDto;
import com.my.relink.domain.trade.dto.TradeRequestDto;
import com.my.relink.domain.trade.dto.TradeRequestResponseDto;
import com.my.relink.service.TradeService;
import com.my.relink.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class TradeController {

    private final TradeService tradeService;


    @GetMapping("/{tradeId}")
    public ResponseEntity<ApiResult<TradeInquiryDetailRespDto>> getTradeInquiryChatRoom(
            @PathVariable("tradeId") Long tradeId,
            @AuthenticationPrincipal AuthUser authUser){
        return ResponseEntity.ok(ApiResult.success(tradeService.getTradeInquiryDetail(tradeId, authUser.toUser())));
    }


    @PostMapping("/trades/{tradeId}/request")
    public ResponseEntity<ApiResult<TradeRequestResponseDto>> requestTrade(@PathVariable(name = "tradeId") Long tradeId, @RequestBody TradeRequestDto tradeRequestDto) {//추후 로그인 유저로 바뀔 얘정
        TradeRequestResponseDto responseDto = tradeService.requestTrade(tradeId, tradeRequestDto.getUserId());
        return new ResponseEntity<>(ApiResult.success(responseDto), HttpStatus.OK);
    }
}
