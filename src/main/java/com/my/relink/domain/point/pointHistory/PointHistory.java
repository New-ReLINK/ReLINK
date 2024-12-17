package com.my.relink.domain.point.pointHistory;


import com.my.relink.domain.BaseEntity;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.trade.Trade;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
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

    public static PointHistory createChargeHistory(Point point){
        return PointHistory.builder()
                .point(point)
                .pointTransactionType(PointTransactionType.CHARGE)
                .amount(point.getAmount())
                .build();
    }

    public static PointHistory create(Integer amount, PointTransactionType pointTransactionType, Point point, Trade trade) {
        PointHistory pointHistory = new PointHistory();
        pointHistory.amount = amount;
        pointHistory.pointTransactionType = pointTransactionType;
        pointHistory.point = point;
        pointHistory.trade = trade;
        return pointHistory;
    }
}
