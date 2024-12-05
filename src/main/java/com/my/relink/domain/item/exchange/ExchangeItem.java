package com.my.relink.domain.item.exchange;

import com.my.relink.domain.BaseEntity;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ExchangeItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String name;

    @Lob
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(value = EnumType.STRING)
    private ItemQuality itemQuality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Integer deposit;

    @Column(length = 20)
    private String size;

    @Column(length = 50)
    private String brand;

    @Column(length = 200)
    private String desiredItem;

    @Enumerated(EnumType.STRING)
    private TradeStatus tradeStatus;

}
