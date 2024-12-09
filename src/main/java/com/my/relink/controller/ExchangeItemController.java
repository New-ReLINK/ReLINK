package com.my.relink.controller;

import com.my.relink.domain.item.exchange.dto.CreateExchangeItemReqDto;
import com.my.relink.service.ExchangeItemService;
import com.my.relink.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ExchangeItemController {

    private final ExchangeItemService exchangeItemService;

    @PostMapping("item/exchange")
    public ResponseEntity<ApiResult<Long>> createExchangeItem(@RequestBody CreateExchangeItemReqDto reqDto) {
        return new ResponseEntity<>(ApiResult.success(exchangeItemService.createExchangeItem(reqDto)), HttpStatus.OK);
    }
}
