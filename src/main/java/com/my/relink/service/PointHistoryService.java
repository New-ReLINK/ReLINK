package com.my.relink.service;

import com.my.relink.controller.point.dto.response.PointUsageHistoryRespDto;
import com.my.relink.domain.point.pointHistory.repository.PointHistoryRepository;
import com.my.relink.domain.user.User;
import com.my.relink.util.DateTimeUtil;
import com.my.relink.util.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;
    private final UserService userService;
    private final DateTimeUtil dateTimeUtil;

    public PageResponse<PointUsageHistoryRespDto> getPointUsageHistories(Long userId, int page, int size) {
        User user = userService.findByIdOrFail(userId);
        List<PointUsageHistoryRespDto> content = convertToResponseDTo(user, page, size);
        return new PageResponse<>(
                content,
                pointHistoryRepository.getPointUsagePageInfo(user, page, size)
        );
    }

    List<PointUsageHistoryRespDto> convertToResponseDTo(User user, int page, int size){
        return pointHistoryRepository.findPointUsageHistories(user, page, size).stream()
                .map(dto -> new PointUsageHistoryRespDto(dto, dateTimeUtil))
                .toList();
    }
}
