package com.my.relink.domain.point.pointHistory.repository;

import com.my.relink.domain.point.pointHistory.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    List<PointHistory> findByTradeIdOrderByCreatedAtDesc(Long tradeId);
}
