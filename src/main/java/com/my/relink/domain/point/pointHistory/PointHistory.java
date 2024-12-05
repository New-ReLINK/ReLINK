package com.my.relink.domain.point.pointHistory;


import com.my.relink.domain.BaseEntity;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.trade.Trade;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PointHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_id")
    private Point point;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id")
    private Trade trade;

    private Integer amount;

    @Enumerated(EnumType.STRING)
    private PointTransactionType pointTransactionType;
}
