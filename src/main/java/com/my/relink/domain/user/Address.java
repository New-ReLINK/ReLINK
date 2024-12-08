package com.my.relink.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@Embeddable
@NoArgsConstructor
public class Address {

    @NotNull(message = "우편번호를 적어주세요.")
    @Min(value = 10000, message = "우편번호 형식이 아닙니다.")
    @Max(value = 99999, message = "우편번호 형식이 아닙니다.")
    private Integer zipcode;

    @NotBlank(message = "기본 주소지를 입력해주세요.")
    @Length(max = 30, message = "기본 주소지는 30자까지 입니다.")
    @Column(length = 30)
    private String baseAddress;

    @NotBlank(message = "상세 주소지를 입력해주세요.")
    @Length(max = 100, message = "상세 주소지는 100자까지 입니다.")
    @Column(length = 100)
    private String detailAddress;

    public Address(Integer zipcode, String baseAddress, String detailAddress) {
        this.zipcode = zipcode;
        this.baseAddress = baseAddress;
        this.detailAddress = detailAddress;
    }
}
