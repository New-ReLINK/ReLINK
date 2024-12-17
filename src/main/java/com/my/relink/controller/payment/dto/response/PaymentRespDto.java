package com.my.relink.controller.payment.dto.response;

import com.my.relink.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PaymentRespDto {
    private Long userId;

    public PaymentRespDto(User user) {
        this.userId = user.getId();
    }
}
