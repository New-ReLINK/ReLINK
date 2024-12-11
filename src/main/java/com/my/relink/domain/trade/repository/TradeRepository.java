package com.my.relink.domain.trade.repository;

import com.my.relink.domain.trade.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;



@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    @Query("select t from Trade t " +
            "join fetch t.ownerExchangeItem oi " +
            "join fetch t.requesterExchangeItem ri " +
            "join fetch oi.user " +
            "join fetch t.requester " +
            "where t.id = :tradeId")
    Optional<Trade> findByIdWithItemsAndUser(@Param("tradeId") Long tradeId);
    @Query("SELECT DISTINCT t FROM Trade t " +
            "JOIN FETCH t.ownerExchangeItem oe " +
            "JOIN FETCH oe.user " +
            "JOIN FETCH t.requesterExchangeItem re " +
            "JOIN FETCH re.user " +
            "WHERE t.ownerExchangeItem.id IN :itemIds OR t.requesterExchangeItem.id IN :itemIds")
    List<Trade> findByExchangeItemIds(@Param("itemIds") List<Long> itemIds);
}
