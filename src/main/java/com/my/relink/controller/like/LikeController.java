package com.my.relink.controller.like;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.like.dto.resp.LikeExchangeItemListRespDto;
import com.my.relink.service.LikeService;
import com.my.relink.util.api.ApiResult;
import com.my.relink.util.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @GetMapping("/users/items/exchanges/likes")
    public ResponseEntity<ApiResult<PageResponse<LikeExchangeItemListRespDto>>> findLikeItem(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResult.success(likeService.getLikeItemList(authUser.getId(), pageable)));
    }

    @PostMapping("/items/exchanges/{itemId}/like")
    public ResponseEntity<ApiResult<Long>> toggleLike(@PathVariable(value = "itemId") Long itemId,
                                                      @AuthenticationPrincipal AuthUser authUser) {
        Long likeId = likeService.toggleLike(authUser.getId(), itemId);
        return ResponseEntity.ok(ApiResult.success(likeId));
    }
}
