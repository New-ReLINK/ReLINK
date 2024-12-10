package com.my.relink.domain.trade;

import com.my.relink.domain.BaseEntity;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.user.Address;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
public class Trade extends BaseEntity {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_exchange_item_id")
    private ExchangeItem ownerExchangeItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_exchange_item_id")
    private ExchangeItem requesterExchangeItem;

    @Enumerated(EnumType.STRING)
    private TradeStatus tradeStatus = TradeStatus.AVAILABLE;

    @Column(length = 40)
    private String ownerTrackingNumber;

    @Column(length = 40)
    private String requesterTrackingNumber;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(column = @Column(name = "owner_base_address"), name = "baseAddress"),
            @AttributeOverride(column = @Column(name = "owner_detail_address"), name = "detailAddress"),
            @AttributeOverride(column = @Column(name = "owner_zipcode"), name = "zipcode")
    })
    private Address ownerAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(column = @Column(name = "requester_base_address"), name = "baseAddress"),
            @AttributeOverride(column = @Column(name = "requester_detail_address"), name = "detailAddress"),
            @AttributeOverride(column = @Column(name = "requester_zipcode"), name = "zipcode")
    })
    private Address requesterAddress;

    @Column(nullable = false)
    private Boolean hasOwnerReceived = false;

    @Column(nullable = false)
    private Boolean hasRequesterReceived = false;

    @Column(nullable = false)
    private Boolean hasOwnerRequested = false;

    @Column(nullable = false)
    private Boolean hasRequesterRequested = false;

    @Enumerated(EnumType.STRING)
    private TradeCancelReason cancelReason;

    public User getOwner(){
        return this.getOwnerExchangeItem().getUser();
    }

    public boolean isParticipant(Long userId){
        return getOwner().getId().equals(userId) || getRequester().getId().equals(userId);
    }

    public void validateAccess(Long userId){
        if(!isParticipant(userId)){
            throw new BusinessException(ErrorCode.TRADE_ACCESS_DENIED);
        }
    }

    public User getPartner(Long userId){
        return getRequester().getId().equals(userId)? getOwner() : getRequester();
    }

    public void updateTradeStatus(TradeStatus tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public void updateHasOwnerRequested(Boolean requestedStatus) {
        this.hasOwnerRequested = requestedStatus;
    }

    public void updateHasRequesterReceived(Boolean requestedStatus) {
        this.hasRequesterReceived = requestedStatus;
    }

    public void updateHasRequesterRequested(Boolean requestedStatus) {
        this.hasRequesterRequested = requestedStatus;
    }

}
