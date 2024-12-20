package com.my.relink.controller.exchangeItem;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.exchangeItem.dto.req.ChoiceExchangeItemReqDto;
import com.my.relink.controller.exchangeItem.dto.req.CreateExchangeItemReqDto;
import com.my.relink.controller.exchangeItem.dto.req.GetAllExchangeItemReqDto;
import com.my.relink.controller.exchangeItem.dto.req.UpdateExchangeItemReqDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetAllExchangeItemsRespDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetExchangeItemRespDto;
import com.my.relink.controller.trade.dto.response.TradeIdRespDto;
import com.my.relink.service.ExchangeItemService;
import com.my.relink.util.api.ApiResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
public class ExchangeItemController {

    private final ExchangeItemService exchangeItemService;

    @PostMapping("/item/exchange")
    public ResponseEntity<ApiResult<Long>> createExchangeItem(@Valid @RequestBody CreateExchangeItemReqDto reqDto,
                                                              @AuthenticationPrincipal AuthUser authUser) {
        Long exchangeItemId = exchangeItemService.createExchangeItem(reqDto, authUser.getId());
        return new ResponseEntity<>(ApiResult.success(exchangeItemId), HttpStatus.OK);
    }

    @GetMapping("/users/items/exchanges")
    public ResponseEntity<ApiResult<GetExchangeItemRespDto>> getExchangeItemsByUserId(@AuthenticationPrincipal AuthUser authUSer,
                                                                                      @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                                                                      @RequestParam(value = "size", required = false, defaultValue = "100") int size) {
        GetExchangeItemRespDto exchangeItems = exchangeItemService.getExchangeItemsByUserId(authUSer.getId(), page, size);
        return new ResponseEntity<>(ApiResult.success(exchangeItems), HttpStatus.OK);
    }

    @GetMapping("/users/items/exchanges/{itemId}")
    public ResponseEntity<ApiResult<GetExchangeItemRespDto>> getExchangeItemModifyPage(@PathVariable(value = "itemId") Long itemId,
                                                                                       @AuthenticationPrincipal AuthUser authUser) {
        GetExchangeItemRespDto respDto = exchangeItemService.getExchangeItemModifyPage(itemId, authUser.getId());
        return new ResponseEntity<>(ApiResult.success(respDto), HttpStatus.OK);
    }

    @PutMapping("/users/items/exchanges/{itemId}")
    public ResponseEntity<ApiResult<Long>> updateExchangeItem(@PathVariable(value = "itemId") Long itemId,
                                                              @Valid @RequestBody UpdateExchangeItemReqDto reqDto,
                                                              @AuthenticationPrincipal AuthUser authUser) {
        Long exchangeItemId = exchangeItemService.updateExchangeItem(itemId, reqDto, authUser.getId());
        return new ResponseEntity<>(ApiResult.success(exchangeItemId), HttpStatus.OK);
    }

    @DeleteMapping("/users/items/exchanges/{itemId}")
    public ResponseEntity<ApiResult<Long>> deleteExchangeItem(@PathVariable(value = "itemId") Long itemId,
                                                              @AuthenticationPrincipal AuthUser authUser) {
        Long exchangeItemId = exchangeItemService.deleteExchangeItem(itemId, authUser.getId());
        return new ResponseEntity<>(ApiResult.success(exchangeItemId), HttpStatus.OK);
    }

    @GetMapping("/items/exchanges")
    public ResponseEntity<ApiResult<GetAllExchangeItemsRespDto>> getAllExchangeItems(@Valid @RequestBody GetAllExchangeItemReqDto reqDto) {
        GetAllExchangeItemsRespDto respDto = exchangeItemService.getAllExchangeItems(reqDto);
        return new ResponseEntity<>(ApiResult.success(respDto), HttpStatus.OK);
    }

    @GetMapping("/items/exchanges/{itemId}")
    public ResponseEntity<ApiResult<GetAllExchangeItemsRespDto>> getExchangeItemFromOwner(@PathVariable(value = "itemId") Long itemId,
                                                                                          @AuthenticationPrincipal AuthUser authUser) {
        GetAllExchangeItemsRespDto respDto = exchangeItemService.getExchangeItemFromOwner(itemId, authUser.getId());
        return new ResponseEntity<>(ApiResult.success(respDto), HttpStatus.OK);
    }

    @GetMapping("/items/exchanges/available")
    public ResponseEntity<ApiResult<GetExchangeItemRespDto>> getExchangeItemChoicePage(@AuthenticationPrincipal AuthUser authUser,
                                                                                       @RequestParam(value = "page", required = false, defaultValue = "0") @Min(0) int page,
                                                                                       @RequestParam(value = "size", required = false, defaultValue = "100") @Min(1) @Max(100) int size) {
        GetExchangeItemRespDto respDto = exchangeItemService.getExchangeItemChoicePage(authUser.getId(), page, size);
        return new ResponseEntity<>(ApiResult.success(respDto), HttpStatus.OK);
    }

    @PostMapping("/items/exchanges/{itemId}/availble")
    public ResponseEntity<ApiResult<TradeIdRespDto>> choiceExchangeItem(@PathVariable(value = "itemId") Long itemId,
                                                                        @Valid @RequestBody ChoiceExchangeItemReqDto reqDto,
                                                                        @AuthenticationPrincipal AuthUser authUser) {
        TradeIdRespDto tradeId = exchangeItemService.choiceExchangeItem(itemId, reqDto, authUser.getId());
        return new ResponseEntity<>(ApiResult.success(tradeId), HttpStatus.OK);
    }
}
