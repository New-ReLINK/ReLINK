package com.my.relink.controller;

import com.my.relink.domain.item.exchange.dto.CreateExchangeItemReqDto;
import com.my.relink.service.ExchangeItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ExchangeItemController {

    private final ExchangeItemService exchangeItemService;

    @PostMapping("item/exchange")
    public ResponseEntity<Long> createExchangeItem(@RequestBody CreateExchangeItemReqDto reqDto) {
        return ResponseEntity.ok(exchangeItemService.createExchangeItem(reqDto));
    }
}
