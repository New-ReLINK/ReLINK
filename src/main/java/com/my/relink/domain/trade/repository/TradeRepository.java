package com.my.relink.domain.trade.repository;

import com.my.relink.domain.trade.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, Long> {
}
