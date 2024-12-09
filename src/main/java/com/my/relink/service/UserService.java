package com.my.relink.service;

import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.ImageRepository;
import com.my.relink.domain.review.ReviewRepository;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.controller.user.dto.req.UserCreateReqDto;
import com.my.relink.controller.user.dto.resp.UserCreateRespDto;
import com.my.relink.controller.user.dto.resp.UserInfoRespDto;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageRepository imageRepository;
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

    public UserCreateRespDto register(UserCreateReqDto dto) {
        dto.changePassword(passwordEncoder.encode(dto.getPassword()));
        User savedUser = userRepository.save(dto.toEntity(dto));
        return new UserCreateRespDto(savedUser.getId());
    }

    public UserInfoRespDto findUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Image image = imageRepository.findByEntityIdAndEntityType(user.getId(), EntityType.USER).orElse(null);

        return new UserInfoRespDto(user, image);
    }
}
