package com.my.relink.controller.exchangeItem;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.exchangeItem.dto.req.CreateExchangeItemReqDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetExchangeItemRespDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetExchangeItemsByUserRespDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetExchangeItemsAndPageByUserRespDto;
import com.my.relink.service.ExchangeItemService;
import com.my.relink.util.api.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResult<GetExchangeItemsAndPageByUserRespDto>> getExchangeItemsByUserId(@AuthenticationPrincipal AuthUser authUSer,
                                                                                                    @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                                                                                    @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        GetExchangeItemsAndPageByUserRespDto exchangeItems = exchangeItemService.getExchangeItemsByUserId(authUSer.getId(), page, size);
        return new ResponseEntity<>(ApiResult.success(exchangeItems), HttpStatus.OK);
    }

    @GetMapping("/users/items/exchanges/{itemId}")
    public ResponseEntity<ApiResult<GetExchangeItemRespDto>> getExchangeItemModifyPage(@PathVariable(value = "itemId") Long itemId,
                                                                                              @AuthenticationPrincipal AuthUser authUser) {
        GetExchangeItemRespDto respDto = exchangeItemService.getExchangeItemModifyPage(itemId, authUser.getId());
        return new ResponseEntity<>(ApiResult.success(respDto), HttpStatus.OK);
    }

}
