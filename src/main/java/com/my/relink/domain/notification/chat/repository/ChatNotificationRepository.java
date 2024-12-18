package com.my.relink.domain.notification.chat.repository;

import com.my.relink.domain.notification.chat.ChatNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatNotificationRepository extends JpaRepository<ChatNotification, Long> {
}
