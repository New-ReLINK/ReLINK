package com.my.relink.controller.user.dto.resp;

import com.my.relink.domain.user.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressRespDto {
    private Integer zipcode;
    private String baseAddress;
    private String detailAddress;

    public UserAddressRespDto(Address address) {
        this.zipcode = address.getZipcode();
        this.baseAddress = address.getBaseAddress();
        this.detailAddress = address.getDetailAddress();
    }
}
