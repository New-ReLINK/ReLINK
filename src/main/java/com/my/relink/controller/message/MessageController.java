package com.my.relink.controller.message;

import com.my.relink.controller.message.dto.response.MessageRespDto;
import com.my.relink.service.MessageService;
import com.my.relink.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/chat/{tradeId}/messages")
    public ResponseEntity<ApiResult<MessageRespDto>> getChatRoomMessages(
            @PathVariable("tradeId") Long tradeId,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "cursor", required = false) Long cursor) {
        return ResponseEntity.ok(ApiResult.success(messageService.getChatRoomMessage(tradeId, size, cursor)));
    }
}
