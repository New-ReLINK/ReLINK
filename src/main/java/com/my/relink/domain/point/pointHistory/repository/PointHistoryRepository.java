package com.my.relink.domain.point.pointHistory.repository;

import com.my.relink.domain.point.pointHistory.PointHistory;
import com.my.relink.domain.point.pointHistory.PointTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long>, PointHistoryCustomRepository {
    Optional<PointHistory> findFirstByTradeIdOrderByCreatedAtDesc(Long tradeId);

    @Query("SELECT ph FROM PointHistory ph " +
            "JOIN ph.trade t " +
            "WHERE t.id = :tradeId AND ph.point.user.id = :userId " +
            "ORDER BY ph.createdAt DESC")
    Optional<PointHistory> findFirstByTradeIdAndUserIdByCreatedAtDesc(@Param("tradeId") Long tradeId, @Param("userId") Long userId);
    boolean existsByTradeIdAndPointUserIdAndPointTransactionType(Long tradeId, Long userId, PointTransactionType pointTransactionType);

}
