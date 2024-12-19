package com.my.relink.domain.trade.repository;

import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface TradeRepository extends JpaRepository<Trade, Long>, CustomTradeRepository {

    @Query("select t from Trade t " +
            "join fetch t.ownerExchangeItem oi " +
            "join fetch t.requesterExchangeItem ri " +
            "left join fetch oi.user " +
            "left join fetch t.requester " +
            "where t.id = :tradeId")
    Optional<Trade> findByIdWithItemsAndUser(@Param("tradeId") Long tradeId);

    @Query("select t from Trade t " +
            "join fetch t.requester " +
            "join fetch t.ownerExchangeItem oi " +
            "join fetch oi.user " +
            "where t.id = :tradeId")
    Optional<Trade> findByIdWithUsers(@Param("tradeId") Long tradeId);

    @Query("SELECT t.id FROM Trade t " +
            "WHERE t.ownerExchangeItem.id = :itemId " +
            "OR t.requesterExchangeItem.id = :itemId")
    Optional<Long> findTradeIdByExchangeItemId(@Param("itemId") Long itemId);
}
