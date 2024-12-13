package com.my.relink.domain.user.repository;

import com.my.relink.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, CustomUserRepository {
    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);


    /**
     *
     * userId가 requester일 때는 ownerExchangeItem의 user를 찾고
     * userId가 ownerExchangeItem의 user일 땐 requester를 찾는다
     *
     */
    @Query(value =
            "select u.* from users u join trade t on t.id = :tradeId " +
                    "where (t.requester_id = :userId and u.id = (select ei.user_id from exchange_item ei where t.owner_exchange_item_id = ei.id)) " +
                    "or " +
                    "(:userId = (select ei.user_id from exchange_item ei where ei.id = t.owner_exchange_item_id) and u.id = t.requester_id)"
            , nativeQuery = true)
    Optional<User> findTradePartnerByUserIdAndTradeId(Long userId, Long tradeId);


}
