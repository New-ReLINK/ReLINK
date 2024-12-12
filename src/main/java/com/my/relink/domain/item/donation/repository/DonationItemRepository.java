package com.my.relink.domain.item.donation.repository;

import com.my.relink.domain.item.donation.DonationItem;
import com.my.relink.domain.item.donation.DonationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationItemRepository extends JpaRepository<DonationItem, Long> {

    long countByDonationStatusAndUserId(DonationStatus donationStatus, Long userId);
}
