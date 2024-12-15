package com.my.relink.controller.notification;

import com.my.relink.common.notification.NotificationEventListener;
import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.notification.dto.resp.NotificationListRespDto;
import com.my.relink.service.NotificationService;
import com.my.relink.util.api.ApiResult;
import com.my.relink.util.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationEventListener notificationEventListener;
    private final NotificationService notificationService;

    @GetMapping(value = "/notifications/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return notificationEventListener.subscribe(authUser.getId());
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResult<PageResponse<NotificationListRespDto>>> getNotifications(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(page = 0, size = 100) Pageable pageable
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResult.success(notificationService.findNotificationList(authUser.getId(), pageable)));
    }
}
