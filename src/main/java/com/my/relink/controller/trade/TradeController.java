package com.my.relink.controller.trade;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.trade.dto.request.AddressReqDto;
import com.my.relink.controller.trade.dto.request.TrackingNumberReqDto;
import com.my.relink.controller.trade.dto.response.*;
import com.my.relink.service.TradeService;
import com.my.relink.util.api.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @GetMapping("/chat/{tradeId}")
    public ResponseEntity<ApiResult<TradeInquiryDetailRespDto>> getTradeInquiryChatRoom(
            @PathVariable("tradeId") Long tradeId,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(ApiResult.success(tradeService.getTradeInquiryDetail(tradeId, authUser.getId())));
    }

    @PostMapping("/trades/{tradeId}/request")
    public ResponseEntity<ApiResult<TradeRequestRespDto>> requestTrade(
            @PathVariable(name = "tradeId") Long tradeId,
            @AuthenticationPrincipal AuthUser authUser) {//추후 로그인 유저로 바뀔 얘정
        TradeRequestRespDto responseDto = tradeService.requestTrade(tradeId, authUser);
        return new ResponseEntity<>(ApiResult.success(responseDto), HttpStatus.OK);
    }

    @PostMapping("/trades/{tradeId}/request-cancel")
    public ResponseEntity<ApiResult<Void>> cancelTradeRequest(
            @PathVariable(name = "tradeId") Long tradeId,
            @AuthenticationPrincipal AuthUser authUser) {
        tradeService.cancelTradeRequest(tradeId, authUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/trades/{tradeId}/completion/address")
    public ResponseEntity<ApiResult<AddressRespDto>> createAddress(
            @PathVariable(name = "tradeId") Long tradeId,
            @RequestBody AddressReqDto reqDto,
            @AuthenticationPrincipal AuthUser authUser) {
        AddressRespDto responseDto = tradeService.createAddress(tradeId, reqDto, authUser);
        return new ResponseEntity<>(ApiResult.success(responseDto), HttpStatus.CREATED);
    }

    @PostMapping("/trades/{tradeId}/completion/received")
    public ResponseEntity<ApiResult<TradeCompleteRespDto>> completeTrade(
            @PathVariable(name = "tradeId") Long tradeId,
            @AuthenticationPrincipal AuthUser authUser) {
        TradeCompleteRespDto responseDto = tradeService.completeTrade(tradeId, authUser);
        return new ResponseEntity<>(ApiResult.success(responseDto), HttpStatus.OK);
    }

    @PostMapping("/trades/{tradeId}/tracking-number")
    public ResponseEntity<ApiResult<Void>> getTrackingNumber(
            @PathVariable(name = "tradeId") Long tradeId,
            @Valid @RequestBody TrackingNumberReqDto reqDto,
            @AuthenticationPrincipal AuthUser authUser) {
        tradeService.getExchangeItemTrackingNumber(tradeId, reqDto, authUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trades/{tradeId}/completion")
    public ResponseEntity<ApiResult<TradeCompletionRespDto>> getCompleteTradeInfo(
            @PathVariable(name = "tradeId") Long tradeId,
            @AuthenticationPrincipal AuthUser authUser) {
        TradeCompletionRespDto responseDto = tradeService.findCompleteTradeInfo(tradeId, authUser);
        return new ResponseEntity<>(ApiResult.success(responseDto), HttpStatus.OK);
    }

    @GetMapping("/trades/{tradeId}/cancel")
    public ResponseEntity<ApiResult<TradeCancelRespDto>> cancelTrade(
            @PathVariable(name = "tradeId") Long tradeId,
            @AuthenticationPrincipal AuthUser authUser) {
        TradeCancelRespDto responseDto = tradeService.cancelTrade(tradeId, authUser);
        return new ResponseEntity<>(ApiResult.success(responseDto), HttpStatus.OK);
    }

}
