package com.my.relink.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Address {

    private Integer zipcode;
    @Column(length = 30)
    private String baseAddress;

    @Column(length = 100)
    private String detailAddress;
}
