package com.my.relink.domain.payment.repository;

import com.my.relink.controller.point.dto.response.PointChargeHistoryRespDto;
import com.my.relink.domain.user.User;
import com.my.relink.util.page.PageResponse;

public interface PaymentCustomRepository {

    PageResponse<PointChargeHistoryRespDto> findPointChargeHistories(User user, int page, int size);
}
