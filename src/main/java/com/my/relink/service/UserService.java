package com.my.relink.service;

import com.my.relink.controller.user.dto.req.*;
import com.my.relink.controller.user.dto.resp.*;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.ImageRepository;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.repository.PointRepository;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageRepository imageRepository;
    private final PointRepository pointRepository;


    public UserCreateRespDto register(UserCreateReqDto dto) {
        dto.changePassword(passwordEncoder.encode(dto.getPassword()));
        User savedUser = userRepository.save(dto.toEntity(dto));

        Point point = Point.builder().amount(0).user(savedUser).build();
        pointRepository.save(point);

        return new UserCreateRespDto(savedUser.getId());
    }

    public UserInfoRespDto findUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Image image = imageRepository.findByEntityIdAndEntityType(user.getId(), EntityType.USER).orElse(null);

        return new UserInfoRespDto(user, image);
    }

    public UserPointRespDto findUserPoint(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Point point = pointRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_NOT_FOUND));

        return new UserPointRespDto(point.getAmount());
    }

    public UserAddressRespDto findAddress(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new UserAddressRespDto(user.getAddress());
    }

    @Transactional
    public void userInfoEdit(Long userId, UserInfoEditReqDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.changeInfo(dto.getName(), dto.getNickname());
    }

    public void deleteUser(UserDeleteReqDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.MISS_MATCHER_PASSWORD);
        }

        user.changeIsDeleted();
    }

    public UserValidNicknameRespDto validNickname(UserValidNicknameRepDto dto) {
        return new UserValidNicknameRespDto(userRepository.findByNickname(dto.getNickname()).isPresent());
    }

    public UserValidEmailRespDto validEmail(UserValidEmailReqDto dto) {
        return new UserValidEmailRespDto(userRepository.findByEmail(dto.getEmail()).isPresent());
    }
}
