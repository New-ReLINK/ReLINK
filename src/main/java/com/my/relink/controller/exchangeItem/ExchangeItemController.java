package com.my.relink.controller.exchangeItem;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.exchangeItem.dto.req.CreateExchangeItemReqDto;
import com.my.relink.service.ExchangeItemService;
import com.my.relink.util.api.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ExchangeItemController {

    private final ExchangeItemService exchangeItemService;

    @PostMapping("/item/exchange")
    public ResponseEntity<ApiResult<Long>> createExchangeItem(@Valid @RequestBody CreateExchangeItemReqDto reqDto,
                                                              @AuthenticationPrincipal AuthUser authUser) {
        Long exchangeItemId = exchangeItemService.createExchangeItem(reqDto, authUser.getId());
        return new ResponseEntity<>(ApiResult.success(exchangeItemId), HttpStatus.OK);
    }

    @GetMapping("/users/items/exchanges")
    public ResponseEntity<ApiResult<Map<String, Object>>> getExchangeItemsByUserId(@AuthenticationPrincipal AuthUser authUSer,
                                                                                   @RequestParam(required = false, defaultValue = "0") int page,
                                                                                   @RequestParam(required = false, defaultValue = "10") int size) {
        Map<String, Object> exchangeItems = exchangeItemService.getExchangeItemsByUserId(authUSer.getId(), page, size);
        return new ResponseEntity<>(ApiResult.success(exchangeItems), HttpStatus.OK);
    }

}
