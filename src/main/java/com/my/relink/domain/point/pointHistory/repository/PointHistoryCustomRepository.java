package com.my.relink.domain.point.pointHistory.repository;

import com.my.relink.controller.point.dto.response.PointUsageHistoryRespDto;
import com.my.relink.domain.user.User;
import com.my.relink.util.page.PageResponse;

public interface PointHistoryCustomRepository {
    PageResponse<PointUsageHistoryRespDto> findPointUsageHistories(User user, int page, int size);
}
