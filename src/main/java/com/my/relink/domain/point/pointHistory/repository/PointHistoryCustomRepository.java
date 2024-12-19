package com.my.relink.domain.point.pointHistory.repository;

import com.my.relink.controller.point.dto.response.PointUsageHistoryRespDto;
import com.my.relink.domain.point.pointHistory.repository.dto.PointUsageHistoryDto;
import com.my.relink.domain.user.User;
import com.my.relink.util.page.PageInfo;
import com.my.relink.util.page.PageResponse;

import java.util.List;

public interface PointHistoryCustomRepository {
    List<PointUsageHistoryDto> findPointUsageHistories(User user, int page, int size);

    PageInfo getPointUsagePageInfo(User user, int page, int size);
}
