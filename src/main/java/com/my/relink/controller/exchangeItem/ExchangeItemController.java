package com.my.relink.controller.exchangeItem;

import com.my.relink.controller.exchangeItem.dto.CreateExchangeItemReqDto;
import com.my.relink.service.ExchangeItemService;
import com.my.relink.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ExchangeItemController {

    private final ExchangeItemService exchangeItemService;

    @PostMapping("/item/exchange")
    public ResponseEntity<ApiResult<Long>> createExchangeItem(@RequestBody CreateExchangeItemReqDto reqDto) {
        // 토큰에서 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        Long exchangeItemId = exchangeItemService.createExchangeItem(reqDto, userId);
        return new ResponseEntity<>(ApiResult.success(exchangeItemId), HttpStatus.OK);
    }
}
