package com.my.relink.controller.point;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.point.dto.response.PointChargeHistoryRespDto;
import com.my.relink.controller.point.dto.response.PointUsageHistoryRespDto;
import com.my.relink.service.PointHistoryService;
import com.my.relink.service.payment.PaymentService;
import com.my.relink.util.api.ApiResult;
import com.my.relink.util.page.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class PointController {

    private final PointHistoryService pointHistoryService;
    private final PaymentService paymentService;

    @GetMapping("/users/point/history/usage")
    public ResponseEntity<ApiResult<PageResponse<PointUsageHistoryRespDto>>> getPointUsageHistories(
                                                    @RequestParam(required = false, defaultValue = "0") @PositiveOrZero int page,
                                                    @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(100) int size,
                                                    @AuthenticationPrincipal AuthUser authUser){
        return ResponseEntity.ok(ApiResult.success(pointHistoryService.getPointUsageHistories(authUser.getId(), page, size)));
    }

    @GetMapping("/users/point/history/charge")
    public ResponseEntity<ApiResult<PageResponse<PointChargeHistoryRespDto>>> getPointChargeHistories(
                                                    @RequestParam(required = false, defaultValue = "0") @PositiveOrZero int page,
                                                    @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(100) int size,
                                                    @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(ApiResult.success(paymentService.getPointChargeHistories(authUser.getId(), page, size)));
    }

}
