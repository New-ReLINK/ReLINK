package com.my.relink.controller.image;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.image.dto.resp.ImageIdRespDto;
import com.my.relink.controller.image.dto.resp.ImageUserProfileCreateRespDto;
import com.my.relink.controller.image.dto.resp.ImageUserProfileDeleteRespDto;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
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

    @PostMapping("/items/donations/{itemId}/images")
    public ResponseEntity<ApiResult<List<Long>>> registerDonationItemImage(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable(value = "itemId") Long itemId,
            @RequestParam List<MultipartFile> files
    ){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResult.success(imageService.addDonationItemImage(authUser.getId(), itemId, files)));
    }

    @PostMapping("/items/exchanges/{itemId}/images")
    public ResponseEntity<ApiResult<List<Long>>> addExchangeItemImage(
            @PathVariable(value = "itemId") Long itemId,
            @RequestParam List<MultipartFile> files,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.NO_IMAGE_UPLOADED);
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResult.success(imageService.addExchangeItemImage(itemId, files, authUser.getId())));
    }

    @DeleteMapping("/items/exchanges/{itemId}/images/{imageId}")
    public ResponseEntity<ApiResult<ImageIdRespDto>> deleteExchangeItemImage(@PathVariable(value = "itemId") Long itemId,
                                                                             @PathVariable(value = "imageId") Long imageId,
                                                                             @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResult.success(imageService.deleteExchangeItemImage(itemId, imageId, authUser.getId())));
    }

    @DeleteMapping("/items/donations/{itemId}/images/{imageId}")
    public ResponseEntity<ApiResult<ImageUserProfileDeleteRespDto>> deleteDonationItemImage(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable(value = "itemId") Long itemId,
            @PathVariable Long imageId
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResult.success(imageService.deleteDonationItemImage(authUser.getId(), itemId, imageId)));
    }
}
