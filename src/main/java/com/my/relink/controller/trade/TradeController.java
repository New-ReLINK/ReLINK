package com.my.relink.controller.trade;

import com.my.relink.controller.trade.dto.response.TradeInquiryDetailRespDto;
import com.my.relink.domain.user.User;
import com.my.relink.service.TradeService;
import com.my.relink.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class TradeController {

    private final TradeService tradeService;

    //TODO: 시큐리티 들어오면 user를 인증 객체로 바꿀것
    @GetMapping("/{tradeId}")
    public ResponseEntity<ApiResult<TradeInquiryDetailRespDto>> getChatRoomInfo(@PathVariable("tradeId") Long tradeId, User user){
        return ResponseEntity.ok(ApiResult.success(tradeService.retrieveTradeDetail(tradeId, user)));
    }
}
