package com.my.relink.domain.message.repository;

import com.my.relink.domain.message.QMessage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomMessageRepositoryImpl implements CustomMessageRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private static final QMessage message = QMessage.message;

    public void deleteMessage(Long tradeId){
        jpaQueryFactory
                .delete(message)
                .where(message.id.eq(tradeId))
                .execute();
    }
}
