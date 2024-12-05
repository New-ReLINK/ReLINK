package com.my.relink.domain.review;


import com.my.relink.domain.BaseEntity;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 2, scale = 1, nullable = false)
    private BigDecimal star;

    @Lob
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_item_id")
    private ExchangeItem exchangeItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private User writer;

    @Enumerated(EnumType.STRING)
    private TradeReview tradeReview;
}
