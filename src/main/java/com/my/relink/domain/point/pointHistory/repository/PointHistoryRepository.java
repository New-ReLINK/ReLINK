package com.my.relink.domain.point.pointHistory.repository;

import com.my.relink.domain.point.pointHistory.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    Optional<PointHistory> findByTradeIdOrderByCreatedAtDesc(Long tradeId);
}
