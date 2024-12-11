package com.my.relink.controller.item.donation.repository;


import com.my.relink.domain.item.donation.DonationItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DonationItemRepository extends JpaRepository<DonationItem, Long> {

    @Query("SELECT d FROM DonationItem d " +
            "WHERE (:category IS NULL OR d.category.name = :category) " +
            "AND (:search IS NULL OR d.name LIKE %:search%) " +
            "ORDER BY d.modifiedAt DESC")

    Page<DonationItem> findAllByFilters(@Param("category") String category,
                                        @Param("search") String search,
                                        Pageable pageable);

    @Query("SELECT COUNT(d) FROM DonationItem d WHERE d.donationStatus = 'DONATION_COMPLETED'")
    long countCompletedDonations();

    @Query("SELECT COUNT(d) FROM DonationItem d WHERE d.donationStatus = 'DONATION_COMPLETED' " +
            "AND FUNCTION('MONTH', d.modifiedAt) = FUNCTION('MONTH', CURRENT_DATE) " +
            "AND FUNCTION('YEAR', d.modifiedAt) = FUNCTION('YEAR', CURRENT_DATE)")
    long countCompletedDonationsThisMonth();
}