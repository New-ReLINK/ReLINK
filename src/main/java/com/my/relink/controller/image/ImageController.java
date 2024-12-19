package com.my.relink.controller.image;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.image.dto.resp.ImageUserProfileCreateRespDto;
import com.my.relink.controller.image.dto.resp.ImageUserProfileDeleteRespDto;
import com.my.relink.service.ImageService;
import com.my.relink.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @DeleteMapping("/users/images/{imageId}")
    public ResponseEntity<ApiResult<ImageUserProfileDeleteRespDto>> deleteUserProfile(
            @PathVariable Long imageId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResult.success(imageService.deleteUserProfile(authUser.getId(), imageId)));
    }

    @PostMapping("/items/exchanges/{itemId}/images")
    public ResponseEntity<ApiResult<List<Long>>> addExchangeItemImage(
            @PathVariable(value = "itemId") Long itemId,
            @RequestParam List<MultipartFile> files,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResult.success(imageService.addExchangeItemImage(itemId, files, authUser.getId())));
    }

    @DeleteMapping("/items/exchanges/{itemId}/images/{imageId}")
    public ResponseEntity<ApiResult<Long>> deleteExchangeItemImage(@PathVariable(value = "itemId") Long itemId,
                                                                   @PathVariable(value = "imageId") Long imageId,
                                                                   @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResult.success(imageService.deleteExchangeItemImage(itemId, imageId, authUser.getId())));
    }
}
