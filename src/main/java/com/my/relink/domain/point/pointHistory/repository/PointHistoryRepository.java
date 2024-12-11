package com.my.relink.domain.point.pointHistory.repository;

import com.my.relink.domain.point.pointHistory.PointHistory;
import com.my.relink.domain.point.pointHistory.PointTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    Optional<PointHistory> findFirstByTradeIdOrderByCreatedAtDesc(Long tradeId);
    boolean existsByTradeIdAndPointUserIdAndPointTransactionType(Long tradeId, Long id, PointTransactionType pointTransactionType);

}
