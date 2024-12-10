package com.my.relink.service;


import com.my.relink.controller.user.dto.resp.UserPointRespDto;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.repository.PointRepository;
import com.my.relink.domain.review.ReviewRepository;
import com.my.relink.domain.user.User;
import com.my.relink.util.DummyObject;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.ImageRepository;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.controller.user.dto.req.AddressCreateReqDto;
import com.my.relink.controller.user.dto.req.UserCreateReqDto;
import com.my.relink.controller.user.dto.resp.UserCreateRespDto;
import com.my.relink.controller.user.dto.resp.UserInfoRespDto;
import com.my.relink.ex.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest extends DummyObject {

    @InjectMocks
    UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private PointRepository pointRepository;


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

    @Test
    @DisplayName("이메일로 사용자 정보를 정상적으로 조회한다")
    void findUserInfo_WithValidEmail_ReturnsUserInfo() {
        // given
        String email = "test@example.com";
        User user = User.builder()
                .id(1L)
                .email(email)
                .name("Test User")
                .nickname("Test Nick")
                .build();

        Image image = Image.builder()
                .id(1L)
                .imageUrl("http://example.com/image.jpg")
                .entityId(user.getId())
                .entityType(EntityType.USER)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(imageRepository.findByEntityIdAndEntityType(user.getId(), EntityType.USER))
                .thenReturn(Optional.of(image));

        // when
        UserInfoRespDto result = userService.findUserInfo(email);

        // then
        assertThat(result).isNotNull();
        assertThat(user.getName()).isEqualTo(result.getName());
        assertThat(user.getNickname()).isEqualTo(result.getNickname());
        assertThat(image.getImageUrl()).isEqualTo(result.getImageUrl());

        verify(userRepository).findByEmail(email);
        verify(imageRepository).findByEntityIdAndEntityType(user.getId(), EntityType.USER);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회시 예외가 발생한다")
    void findUserInfo_WithInvalidEmail_ThrowsException() {
        // given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () -> userService.findUserInfo(email));
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("이미지가 없는 사용자 정보를 정상적으로 조회한다")
    void findUserInfo_WithNoImage_ReturnsUserInfoWithoutImage() {
        // given
        String email = "test@example.com";
        User user = User.builder()
                .id(1L)
                .email(email)
                .name("Test User")
                .nickname("Test Nick")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(imageRepository.findByEntityIdAndEntityType(user.getId(), EntityType.USER))
                .thenReturn(Optional.empty());

        // when
        UserInfoRespDto result = userService.findUserInfo(email);

        // then
        assertThat(result).isNotNull();
        assertThat(user.getName()).isEqualTo(result.getName());
        assertThat(user.getNickname()).isEqualTo(result.getNickname());
        assertThat(result.getImageUrl()).isNull();

        verify(userRepository).findByEmail(email);
        verify(imageRepository).findByEntityIdAndEntityType(user.getId(), EntityType.USER);
    }

    @Test
    @DisplayName("사용자 포인트 조회시 사용자가 없을 경우 USER_NOT_FOUND Exception 이 발생한다.")
    void findPointIsNotFoundUserFailTest() {
        // given
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        // when & then
        assertThrows(BusinessException.class, () -> userService.findUserPoint(userId));
        verify(userRepository, times(1)).findById(any());
    }

    @Test
    @DisplayName("사용자 포인트 조회시 포인트가 조회되지 않을 시 POINT_NOT_FOUND Exception 이 발생한다.")
    void findPointIsNotFoundPointFailTest() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(pointRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () -> userService.findUserPoint(userId));
        verify(userRepository, times(1)).findById(any());
        verify(pointRepository, times(1)).findByUserId(any());
    }

    @Test
    @DisplayName("사용자 포인트 조회 시 정상적으로 수행된다.")
    void findPointSuccessTest() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        Point point = Point.builder()
                .amount(100)
                .user(user)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(pointRepository.findByUserId(userId)).thenReturn(Optional.of(point));

        // when
        UserPointRespDto userPoint = userService.findUserPoint(userId);

        // then
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.getAmount()).isEqualTo(point.getAmount());
        verify(userRepository, times(1)).findById(any());
        verify(pointRepository, times(1)).findByUserId(any());
    }
}