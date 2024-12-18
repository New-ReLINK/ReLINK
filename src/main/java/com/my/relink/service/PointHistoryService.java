package com.my.relink.service;

import com.my.relink.controller.point.dto.response.PointUsageHistoryRespDto;
import com.my.relink.domain.point.pointHistory.repository.PointHistoryRepository;
import com.my.relink.domain.user.User;
import com.my.relink.util.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;
    private final UserService userService;

    public PageResponse<PointUsageHistoryRespDto> getPointUsageHistories(Long userId, int page, int size) {
        User user = userService.findByIdOrFail(userId);
        return pointHistoryRepository.findPointUsageHistories(user, page, size);
    }
}
