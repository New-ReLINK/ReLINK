package com.my.relink.service;

import com.my.relink.domain.user.User;
import com.my.relink.domain.user.UserRepository;
import com.my.relink.dto.req.UserCreateReqDto;
import com.my.relink.dto.resp.UserCreateRespDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserCreateRespDto register(UserCreateReqDto dto) {
        dto.changePassword(passwordEncoder.encode(dto.getPassword()));
        User savedUser = userRepository.save(dto.toEntity(dto));
        return new UserCreateRespDto(savedUser.getId());
    }
}
