package com.my.relink.controller.point;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.point.dto.response.PointUsageHistoryRespDto;
import com.my.relink.service.PointHistoryService;
import com.my.relink.util.api.ApiResult;
import com.my.relink.util.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PointController {

    private final PointHistoryService pointHistoryService;

    @GetMapping("/users/point/history/usage")
    public ResponseEntity<ApiResult<PageResponse<PointUsageHistoryRespDto>>> getPointUsageHistories(
                                                    @RequestParam(required = false, defaultValue = "0") int page,
                                                    @RequestParam(required = false, defaultValue = "10") int size,
                                                    @AuthenticationPrincipal AuthUser authUser){
        return ResponseEntity.ok(ApiResult.success(pointHistoryService.getPointUsageHistories(authUser.getId(), page, size)));
    }

}
