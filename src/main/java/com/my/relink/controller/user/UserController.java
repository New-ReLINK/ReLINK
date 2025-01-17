package com.my.relink.controller.user;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.user.dto.req.*;
import com.my.relink.controller.user.dto.resp.*;
import com.my.relink.service.UserService;
import com.my.relink.util.api.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/auth/signup")
    public ResponseEntity<ApiResult<UserCreateRespDto>> signup(@Valid @RequestBody UserCreateReqDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success(userService.register(dto)));
    }

    @GetMapping("/users/info")
    public ResponseEntity<ApiResult<UserInfoRespDto>> userInfo(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResult.success(userService.findUserInfo(authUser.getEmail())));
    }

    @GetMapping("/users/point")
    public ResponseEntity<ApiResult<UserPointRespDto>> getPoint(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(ApiResult.success(userService.findUserPoint(authUser.getId())));
    }

    @GetMapping("/users/address")
    public ResponseEntity<ApiResult<UserAddressRespDto>> getAddress(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResult.success(userService.findAddress(authUser.getId())));
    }

    @PutMapping("/users/info")
    public ResponseEntity<ApiResult<Void>> editUser(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserInfoEditReqDto dto
    ) {
        userService.userInfoEdit(authUser.getId(), dto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResult.success(null));
    }

    @DeleteMapping("/users")
    public ResponseEntity<ApiResult<Void>> signOut(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserDeleteReqDto dto
    ) {
        userService.deleteUser(authUser.getId(), dto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResult.success(null));
    }

    @PostMapping("/users/check-nickname")
    public ResponseEntity<ApiResult<UserValidNicknameRespDto>> duplicatedNickname(
            @Valid @RequestBody UserValidNicknameRepDto dto
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(ApiResult.success(userService.validNickname(dto)));
    }

    @PostMapping("/users/check-email")
    public ResponseEntity<ApiResult<UserValidEmailRespDto>> duplicatedEmail(@Valid @RequestBody UserValidEmailReqDto dto) {
        return ResponseEntity.status(HttpStatus.OK).body(ApiResult.success(userService.validEmail(dto)));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResult<UserProfileRespDto>> getUserDetailProfile(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResult.success(userService.getUserProfile(authUser.getId())));
    }
}
