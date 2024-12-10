package com.my.relink.controller.user;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.user.dto.req.UserCreateReqDto;
import com.my.relink.controller.user.dto.req.UserValidEmailReqDto;
import com.my.relink.controller.user.dto.req.UserValidNicknameRepDto;
import com.my.relink.controller.user.dto.resp.UserCreateRespDto;
import com.my.relink.controller.user.dto.resp.UserInfoRespDto;
import com.my.relink.controller.user.dto.resp.UserValidEmailRespDto;
import com.my.relink.controller.user.dto.resp.UserValidNicknameRespDto;
import com.my.relink.service.UserService;
import com.my.relink.util.api.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/auth/signup")
    public ResponseEntity<ApiResult<UserCreateRespDto>> signup(@Valid @RequestBody UserCreateReqDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success(userService.register(dto)));
    }

    @GetMapping("/user/info")
    public ResponseEntity<ApiResult<UserInfoRespDto>> userInfo(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResult.success(userService.findUserInfo(authUser.getEmail())));
    }

    @GetMapping("/users/check-nickname")
    public ResponseEntity<ApiResult<UserValidNicknameRespDto>> duplicatedNickname(
            @Valid @RequestBody UserValidNicknameRepDto dto
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(ApiResult.success(userService.validNickname(dto)));
    }

    @GetMapping("/users/check-email")
    public ResponseEntity<ApiResult<UserValidEmailRespDto>> duplicatedEmail(@Valid @RequestBody UserValidEmailReqDto dto) {
        return ResponseEntity.status(HttpStatus.OK).body(ApiResult.success(userService.validEmail(dto)));
    }
}
