package com.my.relink.service;

import com.my.relink.controller.user.dto.resp.UserAddressRespDto;
import com.my.relink.controller.user.dto.req.UserInfoEditReqDto;
import com.my.relink.controller.user.dto.req.UserDeleteReqDto;
import com.my.relink.controller.user.dto.req.UserCreateReqDto;
import com.my.relink.controller.user.dto.req.UserValidEmailReqDto;
import com.my.relink.controller.user.dto.req.UserValidNicknameRepDto;
import com.my.relink.controller.user.dto.resp.UserCreateRespDto;
import com.my.relink.controller.user.dto.resp.UserInfoRespDto;
import com.my.relink.controller.user.dto.resp.UserValidEmailRespDto;
import com.my.relink.controller.user.dto.resp.UserValidNicknameRespDto;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.ImageRepository;
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
