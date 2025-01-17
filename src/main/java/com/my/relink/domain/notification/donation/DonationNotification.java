package com.my.relink.domain.notification.donation;

import com.my.relink.domain.item.donation.DonationStatus;
import com.my.relink.domain.notification.Notification;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@DiscriminatorValue(value = "DONATION")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DonationNotification extends Notification {

    @Column(length = 20)
    private String donationItemName;

    @Enumerated(EnumType.STRING)
    private DonationStatus donationStatus;

    @Builder
    public DonationNotification(Long userId, String donationItemName, DonationStatus donationStatus) {
        super(userId);
        this.donationItemName = donationItemName;
        this.donationStatus = donationStatus;
    }
}
