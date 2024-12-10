package com.my.relink.domain.trade.repository;

import com.my.relink.domain.trade.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    Optional<Trade> findByOwnerExchangeItemIdOrRequesterExchangeItemId(Long ownerExchangeItemId, Long requesterExchangeItemId);
}
