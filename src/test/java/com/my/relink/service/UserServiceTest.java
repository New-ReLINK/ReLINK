package com.my.relink.service;

import com.my.relink.domain.user.User;
import com.my.relink.domain.user.UserRepository;
import com.my.relink.dto.user.req.AddressCreateReqDto;
import com.my.relink.dto.user.req.UserCreateReqDto;
import com.my.relink.dto.user.resp.UserCreateRespDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("정상적인 회원가입 성공")
    void registerSuccess() {

        // given
        UserCreateReqDto reqDto = UserCreateReqDto.builder()
                .name("test")
                .email("test@example.com")
                .nickname("test")
                .contact("010-1111-1111")
                .password("password1234")
                .address(new AddressCreateReqDto(12345, "test", "test"))
                .build();
        User user = reqDto.toEntity(reqDto);

        String encodedPassword = "encodedPassword";
        when(passwordEncoder.encode(any())).thenReturn(encodedPassword);

        User savedUser = User.builder()
                .id(1L)
                .email(user.getEmail())
                .password(encodedPassword)
                .name(user.getName())
                .nickname(user.getNickname())
                .contact(user.getContact())
                .address(user.getAddress())
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        // when
        UserCreateRespDto register = userService.register(reqDto);

        // then
        assertThat(register.userId()).isEqualTo(1L);
        verify(userRepository).save(any());
        verify(passwordEncoder).encode(any());
    }
}