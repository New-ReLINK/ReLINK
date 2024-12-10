package com.my.relink.service;

import com.my.relink.domain.review.repository.ReviewRepository;
import com.my.relink.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserTrustScoreService {

    private final ReviewRepository reviewRepository;


    /**
     * 유저 신뢰도 구하기
     * @param user
     * @return 별점 평균 백분율 전환 값 (정수). 리뷰가 존재하지 않을 경우 0 반환
     */
    public int getTrustScore(User user) {
        Double starAvg = reviewRepository.getTotalStarAvg(user);
        return starAvg == null ? 0 : (int) (starAvg * 20);
    }
}