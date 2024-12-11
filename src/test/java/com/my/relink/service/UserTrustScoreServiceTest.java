package com.my.relink.service;

import com.my.relink.domain.review.repository.ReviewRepository;
import com.my.relink.domain.user.User;
import com.my.relink.util.DummyObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserTrustScoreServiceTest extends DummyObject {

    @InjectMocks
    UserTrustScoreService userTrustScoreService;
    @Mock
    ReviewRepository reviewRepository;

    @Test
    @DisplayName("유저 신뢰도 계산: 정상 케이스")
    void getTrustScore_success(){
        User user = mockRequesterUser();
        when(reviewRepository.getTotalStarAvg(user)).thenReturn(4.5);

        int trustScore = userTrustScoreService.getTrustScore(user);

        assertThat(trustScore).isEqualTo(90);
        verify(reviewRepository, times(1)).getTotalStarAvg(user);
    }

    @Test
    @DisplayName("유저 신뢰도 점수 계산: 리뷰가 없는 경우 신뢰도는 0이어야 한다")
    void getTrustScore_NoReviews() {
        User user = mockRequesterUser();
        when(reviewRepository.getTotalStarAvg(user)).thenReturn(null);

        int trustScore = userTrustScoreService.getTrustScore(user);

        assertThat(trustScore).isEqualTo(0);
        verify(reviewRepository, times(1)).getTotalStarAvg(user);
    }

    @Test
    @DisplayName("유저 신뢰도 점수 계산: 최대 점수는 100이어야 한다")
    void getTrustScore_MaxScore() {
        User user = mockRequesterUser();
        when(reviewRepository.getTotalStarAvg(user)).thenReturn(5.0);

        int trustScore = userTrustScoreService.getTrustScore(user);

        assertThat(trustScore).isEqualTo(100);
        verify(reviewRepository, times(1)).getTotalStarAvg(user);
    }

    @Test
    @DisplayName("유저 신뢰도 점수 계산: 최소 점수는 10이어야 한다")
    void getTrustScore_MinScore() {
        User user = mockRequesterUser();
        when(reviewRepository.getTotalStarAvg(user)).thenReturn(0.5);

        int trustScore = userTrustScoreService.getTrustScore(user);

        assertThat(trustScore).isEqualTo(10);
        verify(reviewRepository, times(1)).getTotalStarAvg(user);
    }

}