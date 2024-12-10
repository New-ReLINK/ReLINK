package com.my.relink.controller.trade;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.trade.dto.response.TradeInquiryDetailRespDto;
import com.my.relink.controller.trade.dto.response.TradeRequestRespDto;
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
        return ResponseEntity.ok(ApiResult.success(tradeService.getTradeInquiryDetail(tradeId, authUser.getId())));
    }


    @PostMapping("/trades/{tradeId}/request")
    public ResponseEntity<ApiResult<TradeRequestRespDto>> requestTrade(@PathVariable(name = "tradeId") Long tradeId, @AuthenticationPrincipal AuthUser authUser) {//추후 로그인 유저로 바뀔 얘정
        TradeRequestRespDto responseDto = tradeService.requestTrade(tradeId, authUser);
        return new ResponseEntity<>(ApiResult.success(responseDto), HttpStatus.OK);
    }

    @PostMapping("/trades/{tradeId}/request-cancel")
    public ResponseEntity<Void> cancelTradeRequest(@PathVariable(name = "tradeId") Long tradeId, @AuthenticationPrincipal AuthUser authUser) {
        tradeService.cancelTradeRequest(tradeId, authUser);
        return ResponseEntity.noContent().build();
    }

}
