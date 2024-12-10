package com.my.relink.domain.trade.repository;

import com.my.relink.domain.trade.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    @Query("select t from Trade t " +
            "join fetch t.ownerExchangeItem oi " +
            "join fetch t.requesterExchangeItem ri " +
            "join fetch oi.user " +
            "join fetch t.requester " +
            "where t.id = :tradeId")
    Optional<Trade> findByIdWithItemsAndUser(@Param("tradeId") Long tradeId);
    Optional<Trade> findByOwnerExchangeItemIdOrRequesterExchangeItemId(Long ownerExchangeItemId, Long requesterExchangeItemId);
}
