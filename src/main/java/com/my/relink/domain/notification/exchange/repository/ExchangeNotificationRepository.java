package com.my.relink.domain.notification.exchange.repository;

import com.my.relink.domain.notification.exchange.ExchangeNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeNotificationRepository extends JpaRepository<ExchangeNotification, Long> {
}
