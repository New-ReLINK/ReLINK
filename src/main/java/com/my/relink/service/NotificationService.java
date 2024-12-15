package com.my.relink.service;

import com.my.relink.controller.notification.dto.resp.NotificationListRespDto;
import com.my.relink.controller.notification.mapper.NotificationListMapper;
import com.my.relink.domain.notification.Notification;
import com.my.relink.domain.notification.repository.NotificationRepository;
import com.my.relink.util.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public PageResponse<NotificationListRespDto> findNotificationList(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        Page<NotificationListRespDto> notificationDtos = NotificationListMapper.from(notifications, pageable);
        return PageResponse.of(notificationDtos);
    }
}
