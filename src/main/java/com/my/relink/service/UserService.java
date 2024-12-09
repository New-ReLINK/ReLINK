package com.my.relink.service;

import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.ImageRepository;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.UserRepository;
import com.my.relink.dto.user.req.UserCreateReqDto;
import com.my.relink.dto.user.resp.UserCreateRespDto;
import com.my.relink.dto.user.resp.UserInfoRespDto;
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
