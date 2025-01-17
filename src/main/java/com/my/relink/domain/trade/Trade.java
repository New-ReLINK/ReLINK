package com.my.relink.domain.trade;

import com.my.relink.domain.BaseEntity;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.user.Address;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Trade extends BaseEntity {

    @Id
    @GeneratedValue
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

    @Lob
    private String tradeCancelDescription;

    public ExchangeItem getPartnerItem(Long userId){
        return userId.equals(getOwner().getId()) ?
                requesterExchangeItem :
                ownerExchangeItem;
    }


    public User getOwner() {
        return this.getOwnerExchangeItem().getUser();
    }

    public boolean isParticipant(Long userId) {
        return getOwner().getId().equals(userId) || this.requester.getId().equals(userId);
    }

    public void validateAccess(Long userId) {
        if (!isParticipant(userId)) {
            throw new BusinessException(ErrorCode.TRADE_ACCESS_DENIED);
        }
    }

    public User getPartner(Long userId) {
        return getRequester().getId().equals(userId) ? getOwner() : this.requester;
    }

    public void updateTradeStatus(TradeStatus tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public void updateHasOwnerRequested(Boolean requestedStatus) {
        this.hasOwnerRequested = requestedStatus;
    }

    public void updateHasOwnerReceived(Boolean requestedStatus) {
        this.hasOwnerReceived = requestedStatus;
    }

    public void updateHasRequesterReceived(Boolean requestedStatus) {
        this.hasRequesterReceived = requestedStatus;
    }

    public void updateHasRequesterRequested(Boolean requestedStatus) {
        this.hasRequesterRequested = requestedStatus;
    }

    public void saveOwnerAddress(Address newAddress) {
        this.ownerAddress = newAddress;
    }

    public void saveRequesterAddress(Address newAddress) {
        this.requesterAddress = newAddress;
    }

    public void updateRequesterTrackingNumber(String requesterTrackingNumber) {
        this.requesterTrackingNumber = requesterTrackingNumber;
    }

    public void updateOwnerTrackingNumber(String ownerTrackingNumber) {
        this.ownerTrackingNumber = ownerTrackingNumber;
    }

    public boolean isRequester(Long userId) {
        return this.requester.getId().equals(userId);
    }

    public boolean isTradeInExchange(Trade trade){
        return trade.getTradeStatus() == TradeStatus.IN_EXCHANGE;
    }

    public ExchangeItem getMyExchangeItem(Long myUserId) {
        if (isRequester(myUserId)) {
            return requesterExchangeItem;
        } else {
            return ownerExchangeItem;
        }
    }

    public ExchangeItem getPartnerExchangeItem(Long myUserId) {
        if (isRequester(myUserId)) {
            return ownerExchangeItem;
        } else {
            return requesterExchangeItem;
        }
    }

    @Builder
    public Trade(
            Long id,
            User requester,
            ExchangeItem ownerExchangeItem,
            ExchangeItem requesterExchangeItem,
            TradeStatus tradeStatus,
            String ownerTrackingNumber,
            String requesterTrackingNumber,
            Address ownerAddress,
            Address requesterAddress,
            Boolean hasOwnerReceived,
            Boolean hasRequesterReceived,
            Boolean hasOwnerRequested,
            Boolean hasRequesterRequested,
            TradeCancelReason cancelReason
    ) {
        this.id = id;
        this.requester = requester;
        this.ownerExchangeItem = ownerExchangeItem;
        this.requesterExchangeItem = requesterExchangeItem;
        this.tradeStatus = tradeStatus;
        this.ownerTrackingNumber = ownerTrackingNumber;
        this.requesterTrackingNumber = requesterTrackingNumber;
        this.ownerAddress = ownerAddress;
        this.requesterAddress = requesterAddress;
        this.hasOwnerReceived = hasOwnerReceived;
        this.hasRequesterReceived = hasRequesterReceived;
        this.hasOwnerRequested = hasOwnerRequested;
        this.hasRequesterRequested = hasRequesterRequested;
        this.cancelReason = cancelReason;
    }

    public void updateTradeCancelReason(TradeCancelReason tradeCancelReason, String tradeCancelDescription) {
        this.cancelReason = tradeCancelReason;
        this.tradeCancelDescription = tradeCancelDescription;
    }
}
