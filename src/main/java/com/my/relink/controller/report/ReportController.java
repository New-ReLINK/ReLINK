package com.my.relink.controller.report;

import com.my.relink.config.security.AuthUser;
import com.my.relink.service.ReportService;
import com.my.relink.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/trades/{tradeId}/report")
    public ResponseEntity<ApiResult<Void>> createTradeReport(@PathVariable("tradeId") Long tradeId,
                                                             @AuthenticationPrincipal AuthUser authUser){
        reportService.createTradeReport(tradeId, authUser.getId());
        return ResponseEntity.ok(ApiResult.success(null));
    }


}
