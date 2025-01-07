package com.my.relink.domain.item.exchange;

import com.my.relink.domain.BaseEntity;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@Table(indexes = {
        @Index(
                name = "idx_exchange_item_search",
                columnList = "name,trade_status,created_at DESC,id DESC"
        )
})

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
    private boolean isDeleted = false;

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
            boolean isDeleted
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

    public void updateStatus(TradeStatus tradeStatus) {
        this.tradeStatus = tradeStatus;
    }
    public void validExchangeItemOwner(Long itemOwnerId, Long userId){
        if (!itemOwnerId.equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    public void update(String name, String description, Category category,
                       ItemQuality itemQuality, String size, String brand, String desiredItem, Integer deposit) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.itemQuality = itemQuality;
        this.size = size;
        this.brand = brand;
        this.desiredItem = desiredItem;
        this.deposit = deposit;
    }
    public void delete() {
        this.isDeleted = true;
    }
}
