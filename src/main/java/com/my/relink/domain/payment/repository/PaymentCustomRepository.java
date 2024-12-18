package com.my.relink.domain.payment.repository;

import com.my.relink.controller.point.dto.response.PointChargeHistoryRespDto;
import com.my.relink.domain.payment.repository.dto.PointChargeHistoryDto;
import com.my.relink.domain.user.User;
import com.my.relink.util.page.PageInfo;
import com.my.relink.util.page.PageResponse;

import java.util.List;

public interface PaymentCustomRepository {

    List<PointChargeHistoryDto> findPointChargeHistories(User user, int page, int size);

    PageInfo getPointChargePageInfo(User user, int page, int size);
}
