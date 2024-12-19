package com.my.relink.domain.trade.repository;

import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface TradeRepository extends JpaRepository<Trade, Long>, CustomTradeRepository {


    @Query("SELECT t FROM Trade t " +
            "JOIN FETCH t.ownerExchangeItem ei " +
            "WHERE t.id = :tradeId")
    Optional<Trade> findByIdWithOwnerItem(@Param("tradeId") Long tradeId);


    @Query("select t from Trade t " +
            "join fetch t.ownerExchangeItem oi " +
            "join fetch t.requesterExchangeItem ri " +
            "left join fetch oi.user " +
            "left join fetch t.requester " +
            "where t.id = :tradeId")
    Optional<Trade> findByIdWithItemsAndUser(@Param("tradeId") Long tradeId);

    @Query("SELECT t FROM Trade t " +
            "LEFT JOIN FETCH t.requester r " +
            "LEFT JOIN FETCH t.requesterExchangeItem rei " +
            "LEFT JOIN FETCH t.ownerExchangeItem oei " +
            "WHERE t.id = :tradeId")
    Optional<Trade> findTradeWithDetails(@Param("tradeId") Long tradeId);

    @Query("select t from Trade t " +
            "join fetch t.requester " +
            "join fetch t.ownerExchangeItem oi " +
            "join fetch oi.user " +
            "where t.id = :tradeId")
    Optional<Trade> findByIdWithUsers(@Param("tradeId") Long tradeId);

    boolean existsByRequesterIdAndTradeStatus(Long userId, TradeStatus tradeStatus);

    @Query("SELECT t FROM Trade t " +
            "JOIN FETCH t.ownerExchangeItem oei " + // 거래의 owner가 등록한 아이템 정보
            "JOIN FETCH t.requesterExchangeItem rei " + // 거래의 partner가 등록한 아이템 정보
            "WHERE t.id = :tradeId")
    Optional<Trade> findByIdWithExchangeItem(@Param("tradeId") Long tradeId);

    @Query("SELECT t.id FROM Trade t " +
            "WHERE t.ownerExchangeItem.id = :itemId " +
            "OR t.requesterExchangeItem.id = :itemId")
    Optional<Long> findTradeIdByExchangeItemId(@Param("itemId") Long itemId);
}
