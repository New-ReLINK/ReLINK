package com.my.relink.controller.trade;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.trade.dto.response.MessageRespDto;
import com.my.relink.controller.trade.dto.response.TradeInquiryDetailRespDto;
import com.my.relink.controller.trade.dto.request.TradeReqDto;
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


    @PostMapping("/trades/{tradeId}/request")
    public ResponseEntity<ApiResult<TradeRequestRespDto>> requestTrade(@PathVariable(name = "tradeId") Long tradeId, @RequestBody TradeReqDto tradeReqDto) {//추후 로그인 유저로 바뀔 얘정
        TradeRequestRespDto responseDto = tradeService.requestTrade(tradeId, tradeReqDto.getUserId());
        return new ResponseEntity<>(ApiResult.success(responseDto), HttpStatus.OK);
    }


    @GetMapping("/{tradeId}")
    public ResponseEntity<ApiResult<TradeInquiryDetailRespDto>> getTradeInquiryChatRoom(
            @PathVariable("tradeId") Long tradeId,
            @AuthenticationPrincipal AuthUser authUser){
        return ResponseEntity.ok(ApiResult.success(tradeService.getTradeInquiryDetail(tradeId, authUser.toUser())));
    }


    @GetMapping("/{tradeId}/messages")
    public ResponseEntity<ApiResult<MessageRespDto>> getChatRoomMessages(
            @PathVariable("tradeId") Long tradeId,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "cursor", required = false) Long cursor) {
        return ResponseEntity.ok(ApiResult.success(tradeService.getChatRoomMessage(tradeId, size, cursor)));
    }
}
