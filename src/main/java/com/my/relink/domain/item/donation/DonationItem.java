package com.my.relink.domain.item.donation;


import com.my.relink.domain.BaseEntity;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.user.Address;
import com.my.relink.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DonationItem extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
}
