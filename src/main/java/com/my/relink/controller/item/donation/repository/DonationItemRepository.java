package com.my.relink.controller.item.donation.repository;


import com.my.relink.domain.item.donation.DonationItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationItemRepository extends JpaRepository<DonationItem, Long> {

}