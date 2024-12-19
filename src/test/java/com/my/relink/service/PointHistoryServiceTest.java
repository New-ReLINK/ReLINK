package com.my.relink.service;

import com.my.relink.controller.point.dto.response.PointUsageHistoryRespDto;
import com.my.relink.domain.point.pointHistory.repository.PointHistoryRepository;
import com.my.relink.domain.point.pointHistory.repository.dto.PointUsageHistoryDto;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.util.DateTimeUtil;
import com.my.relink.util.page.PageInfo;
import com.my.relink.util.page.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointHistoryServiceTest {

    @InjectMocks
    private PointHistoryService pointHistoryService;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Mock
    private UserService userService;
    @Mock
    private DateTimeUtil dateTimeUtil;

    @Nested
    @DisplayName("포인트 사용 내역 조회 테스트")
    class GetPointUsageHistories{
        private User user;
        private Long userId;
        private int page = 0;
        private int size = 10;
        private List<PointUsageHistoryDto> response;



        @Test
        @DisplayName("포인트 사용 내역 조회에 성공한다")
        void success(){
            user = mock(User.class);
            response  = List.of(
                    new PointUsageHistoryDto(
                            1L,
                            "아이템 1",
                            TradeStatus.EXCHANGED,
                            10000,
                            10000,
                            LocalDateTime.now(),
                            LocalDateTime.now()
                    ),
                    new PointUsageHistoryDto(
                            2L,
                            "아이템 2",
                            TradeStatus.EXCHANGED,
                            10000,
                            10000,
                            LocalDateTime.now(),
                            LocalDateTime.now()
                    )
            );
            when(userService.findByIdOrFail(userId)).thenReturn(user);
            when(pointHistoryRepository.findPointUsageHistories(user, page, size))
                    .thenReturn(response);
            when(pointHistoryRepository.getPointUsagePageInfo(eq(user), eq(0), eq(10)))
                    .thenReturn(new PageInfo(1, 2, false, false));

            PageResponse<PointUsageHistoryRespDto> result = pointHistoryService.getPointUsageHistories(userId, page, size);

            assertEquals(result.getContent().size(), response.size());
            verify(userService).findByIdOrFail(userId);
            verify(pointHistoryRepository).findPointUsageHistories(user, page, size);
        }

        @Test
        @DisplayName("존재하지 않는 사용자일 경우 예외가 발생한다")
        void fail_when_userNotFound(){
            when(userService.findByIdOrFail(userId))
                    .thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            assertThatThrownBy(() -> pointHistoryService.getPointUsageHistories(userId, page, size))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

            verify(pointHistoryRepository, never()).findPointUsageHistories(any(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("내역이 없는 경우 빈 리스트를 반환한다")
        void getPointUsageHistories_EmptyList() {
            when(userService.findByIdOrFail(userId)).thenReturn(user);
            when(pointHistoryRepository.findPointUsageHistories(user, page, size))
                    .thenReturn(List.of());
            when(pointHistoryRepository.getPointUsagePageInfo(eq(user), eq(0), eq(10)))
                    .thenReturn(new PageInfo(1, 2, false, false));

            PageResponse<PointUsageHistoryRespDto> result =
                    pointHistoryService.getPointUsageHistories(userId, 0, 10);

            assertThat(result.getContent()).isEmpty();
        }

    }

}