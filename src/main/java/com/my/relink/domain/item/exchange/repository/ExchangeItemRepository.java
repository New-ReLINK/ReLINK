package com.my.relink.domain.item.exchange.repository;

import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.Optional;

public interface ExchangeItemRepository extends JpaRepository<ExchangeItem, Long> {

    @Query(value = """
            select ei.*
            from exchange_item ei
            where ei.user_id = :userId
            """
            ,nativeQuery = true)
    Optional<ExchangeItem> findByUserIdIncludingWithdrawn(@Param("userId") Long userId);
}
