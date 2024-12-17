package com.my.relink.controller.image;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.image.dto.resp.ImageUserProfileCreateRespDto;
import com.my.relink.service.ImageService;
import com.my.relink.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/users/image")
    public ResponseEntity<ApiResult<ImageUserProfileCreateRespDto>> registerUserProfile(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam MultipartFile file
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResult.success(imageService.addUserProfile(authUser.getId(), file)));
    }
}
