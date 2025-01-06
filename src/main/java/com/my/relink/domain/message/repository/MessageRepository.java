package com.my.relink.domain.message.repository;

import com.my.relink.domain.message.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long>, CustomMessageRepository {

    @Query("select m from Message m " +
            "join fetch m.user " +
            "where m.trade.id = :tradeId " +
            "and m.messageTime < :cursor " +
            "order by m.messageTime desc")
    List<Message> findMessagesBeforeCursor(@Param("tradeId") Long tradeId, @Param("cursor") LocalDateTime cursor, Pageable pageable);
}
