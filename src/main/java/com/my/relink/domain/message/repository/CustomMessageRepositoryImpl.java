package com.my.relink.domain.message.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.my.relink.domain.message.QMessage.message;

@Repository
@RequiredArgsConstructor
public class CustomMessageRepositoryImpl implements CustomMessageRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public void deleteMessage(Long tradeId) {
        jpaQueryFactory
                .delete(message)
                .where(message.id.eq(tradeId))
                .execute();
    }
}
