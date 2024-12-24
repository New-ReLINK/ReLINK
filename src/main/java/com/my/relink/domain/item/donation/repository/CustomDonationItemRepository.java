package com.my.relink.domain.item.donation.repository;


import com.my.relink.domain.item.donation.DonationItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CustomDonationItemRepository {
    Page<DonationItem> findAllByFilters(String category, String search, Pageable pageable);
    long countCompletedDonations();
    long countCompletedDonationsThisMonth();
    Optional<DonationItem> findByIdWithCategory(Long id);
}
