package com.my.relink.domain.item.donation.repository;


import com.my.relink.domain.item.donation.DonationItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationItemRepository extends JpaRepository<DonationItem, Long>, CustomDonationItemRepository {

    Page<DonationItem> findByUserId(Long userId, Pageable pageable);
    boolean existsByIdAndUserId(Long itemId, Long userId);
}
