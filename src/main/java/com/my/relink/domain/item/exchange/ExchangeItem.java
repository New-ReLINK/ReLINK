package com.my.relink.domain.item.exchange;

import com.my.relink.domain.BaseEntity;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
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

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Builder
    public ExchangeItem(
            Long id,
            String name,
            String description,
            Category category,
            ItemQuality itemQuality,
            User user,
            Integer deposit,
            String size,
            String brand,
            String desiredItem,
            TradeStatus tradeStatus,
            Boolean isDeleted
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.itemQuality = itemQuality;
        this.user = user;
        this.deposit = deposit;
        this.size = size;
        this.brand = brand;
        this.desiredItem = desiredItem;
        this.tradeStatus = tradeStatus;
        this.isDeleted = isDeleted;
    }
}
