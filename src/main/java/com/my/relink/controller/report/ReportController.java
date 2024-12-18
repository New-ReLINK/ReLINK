package com.my.relink.controller.report;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.report.dto.request.ExchangeItemReportCreateReqDto;
import com.my.relink.controller.report.dto.request.TradeReportCreateReqDto;
import com.my.relink.controller.report.dto.response.ExchangeItemInfoRespDto;
import com.my.relink.controller.report.dto.response.TradeInfoRespDto;
import com.my.relink.service.ReportService;
import com.my.relink.util.api.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/items/exchanges/{itemId}/report")
    public ResponseEntity<ApiResult<ExchangeItemInfoRespDto>> getExchangeItemInfoForReport(@PathVariable("itemId") Long itemId){
        return ResponseEntity.ok(ApiResult.success(reportService.getExchangeItemInfoForReport(itemId)));
    }

    @GetMapping("/trades/{tradeId}/report")
    public ResponseEntity<ApiResult<TradeInfoRespDto>> getTradeInfoForReport(@PathVariable("tradeId") Long tradeId,
                                                                    @AuthenticationPrincipal AuthUser authUser){
        return ResponseEntity.ok(ApiResult.success(reportService.getTradeInfoForReport(tradeId, authUser.getId())));
    }

    @PostMapping("/trades/{tradeId}/report")
    public ResponseEntity<ApiResult<Void>> createTradeReport(@PathVariable("tradeId") Long tradeId,
                                                             @RequestBody @Valid TradeReportCreateReqDto tradeReportCreateReqDto,
                                                             @AuthenticationPrincipal AuthUser authUser){
        reportService.createTradeReport(tradeId, authUser.getId(), tradeReportCreateReqDto);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @PostMapping("/items/exchanges/{itemId}/report")
    public ResponseEntity<ApiResult<Void>> createExchangeItemReport(@PathVariable("itemId") Long itemId,
                                                                    @RequestBody @Valid ExchangeItemReportCreateReqDto exchangeItemReportCreateReqDto){
        reportService.createExchangeItemReport(itemId, exchangeItemReportCreateReqDto);
        return ResponseEntity.ok(ApiResult.success(null));
    }


}
