package com.my.relink.domain.like.repository;

import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.like.Like;
import com.my.relink.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long>, CustomLikeRepository {
    Optional<Like> findByUserAndExchangeItem(User user, ExchangeItem exchangeItem);

    void deleteByExchangeItemId(Long itemId);

    Boolean existsByExchangeItem_IdAndUser_Id(Long itemId, Long userId);
}
