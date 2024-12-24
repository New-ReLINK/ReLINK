package com.my.relink.domain.item.donation.repository;


import com.my.relink.domain.item.donation.DonationItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomDonationItemRepository {
    Page<DonationItem> findAllByFilters(String category, String search, Pageable pageable);
}
