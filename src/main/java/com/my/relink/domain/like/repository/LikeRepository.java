package com.my.relink.domain.like.repository;

import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.like.Like;
import com.my.relink.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long>, CustomLikeRepository {
    Optional<Like> findByUserAndExchangeItem(User user, ExchangeItem exchangeItem);

    void deleteByExchangeItemId(Long itemId);
}
