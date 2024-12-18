package com.my.relink.service;

import com.my.relink.controller.point.dto.response.PointUsageHistoryRespDto;
import com.my.relink.domain.point.pointHistory.repository.PointHistoryRepository;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.util.page.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageChannel;

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

    @Nested
    @DisplayName("포인트 사용 내역 조회 테스트")
    class GetPointUsageHistories{
        private User user;
        private Long userId;
        private int page = 0;
        private int size = 10;
        private PageResponse<PointUsageHistoryRespDto> response;


        @Test
        @DisplayName("포인트 사용 내역 조회에 성공한다")
        void success(){
            user = mock(User.class);
            response = mock(PageResponse.class);
            when(userService.findByIdOrFail(userId)).thenReturn(user);
            when(pointHistoryRepository.findPointUsageHistories(user, page, size))
                    .thenReturn(response);

            PageResponse<PointUsageHistoryRespDto> result = pointHistoryService.getPointUsageHistories(userId, page, size);

            assertThat(result).isEqualTo(response);
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

    }

}