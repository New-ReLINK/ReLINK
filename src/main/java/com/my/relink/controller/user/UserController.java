package com.my.relink.controller.user;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.user.dto.req.UserCreateReqDto;
import com.my.relink.controller.user.dto.req.UserInfoEditReqDto;
import com.my.relink.controller.user.dto.resp.UserCreateRespDto;
import com.my.relink.controller.user.dto.resp.UserInfoRespDto;
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

    @PutMapping("/users/info")
    public ResponseEntity<ApiResult<Void>> editUser(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserInfoEditReqDto dto
    ) {
        userService.userInfoEdit(authUser.getId(), dto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResult.success(null));
    }
}
