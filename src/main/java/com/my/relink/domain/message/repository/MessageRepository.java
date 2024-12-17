package com.my.relink.domain.message.repository;

import com.my.relink.domain.message.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("select m from Message m " +
            "join fetch m.user " +
            "where m.trade.id = :tradeId " +
            "and m.id < :cursor " +
            "order by m.id desc")
    List<Message> findMessagesBeforeCursor(@Param("tradeId")Long tradeId, @Param("cursor") Long cursor, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.trade.id = :tradeId")
    void deleteByTradeId(@Param("tradeId") Long tradeId);
}
