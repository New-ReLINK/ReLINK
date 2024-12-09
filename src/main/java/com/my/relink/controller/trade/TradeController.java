package com.my.relink.controller.trade;

import com.my.relink.controller.trade.dto.response.TradeResponse;
import com.my.relink.domain.user.User;
import com.my.relink.service.ChatService;
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

    private final ChatService chatService;

    @GetMapping("/{tradeId}")
    public ResponseEntity<ApiResult<TradeResponse>> getChatRoomInfo(@PathVariable("tradeId") Long tradeId, User user){
        return ResponseEntity.ok(ApiResult.success(chatService.getChatRoomInfo(tradeId, user)));
    }
}
