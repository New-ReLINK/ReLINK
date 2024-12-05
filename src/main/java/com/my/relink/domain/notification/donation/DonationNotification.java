package com.my.relink.domain.notification.donation;

import com.my.relink.domain.history.donation.status.DonationStatus;
import com.my.relink.domain.notification.Notification;
import jakarta.persistence.*;
import lombok.AccessLevel;
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
}
