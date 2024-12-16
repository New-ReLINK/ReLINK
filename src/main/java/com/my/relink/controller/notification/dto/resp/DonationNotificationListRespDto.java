package com.my.relink.controller.notification.dto.resp;

import com.my.relink.domain.item.donation.DonationStatus;
import com.my.relink.domain.notification.donation.DonationNotification;
import lombok.Getter;

@Getter
public class DonationNotificationListRespDto extends NotificationListRespDto {
    private final String itemName;
    private final DonationStatus status;

    public DonationNotificationListRespDto(DonationNotification notification) {
        super(notification.getCreatedAt(), "DONATION");
        this.itemName = notification.getDonationItemName();
        this.status = notification.getDonationStatus();
    }
}
