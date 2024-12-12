package com.my.relink.domain.trade.repository;

import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.trade.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT t FROM Trade t " +
            "LEFT JOIN FETCH t.requester r " +
            "LEFT JOIN FETCH t.requesterExchangeItem rei " +
            "LEFT JOIN FETCH t.ownerExchangeItem oei " +
            "LEFT JOIN FETCH Image reiImg ON reiImg.entityId = rei.id AND reiImg.entityType = :entityType " +
            "LEFT JOIN FETCH Image oeiImg ON oeiImg.entityId = oei.id AND oeiImg.entityType = :entityType " +
            "WHERE t.id = :tradeId")
    Optional<Trade> findTradeWithDetails(@Param("tradeId") Long tradeId, @Param("entityType") EntityType entityType);
}
