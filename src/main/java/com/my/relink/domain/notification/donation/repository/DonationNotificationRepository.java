package com.my.relink.domain.notification.donation.repository;

import com.my.relink.domain.notification.donation.DonationNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationNotificationRepository extends JpaRepository<DonationNotification, Long> {
}
