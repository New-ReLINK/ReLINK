package com.my.relink.domain.item.donation;

import com.my.relink.domain.BaseEntity;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.user.Address;
import com.my.relink.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DonationItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String name;

    @Lob
    private String description;

    @Column(length = 128)
    private String desiredDestination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Lob
    private String detailRejectedReason;

    @Column(length = 128)
    private String certificateUrl;

    @Column(length = 128)
    private String destination;

    @Column(length = 20)
    private String size;

    @Embedded
    private Address returnAddress;

    @Enumerated(value = EnumType.STRING)
    private ItemQuality itemQuality;

    @Enumerated(value = EnumType.STRING)
    private DonationStatus donationStatus;

    @Enumerated(value = EnumType.STRING)
    private RejectedReason rejectedReason;

    @Enumerated(value = EnumType.STRING)
    private DisposalType disposalType;


    @Builder
    public DonationItem(
            Long id,
            String name,
            String description,
            String desiredDestination,
            User user,
            Category category,
            String detailRejectedReason,
            String certificateUrl,
            String destination,
            String size,
            Address returnAddress,
            ItemQuality itemQuality,
            DonationStatus donationStatus,
            RejectedReason rejectedReason,
            DisposalType disposalType
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.desiredDestination = desiredDestination;
        this.user = user;
        this.category = category;
        this.detailRejectedReason = detailRejectedReason;
        this.certificateUrl = certificateUrl;
        this.destination = destination;
        this.size = size;
        this.returnAddress = returnAddress;
        this.itemQuality = itemQuality;
        this.donationStatus = donationStatus;
        this.rejectedReason = rejectedReason;
        this.disposalType = disposalType;
    }

}
