package com.my.relink.controller.donation;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.donation.dto.resp.*;
import com.my.relink.controller.donation.dto.req.DonationItemReqDto;
import com.my.relink.service.DonationItemService;
import com.my.relink.util.api.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DonationController {
    private final DonationItemService donationItemService;

    @PostMapping("/item/donation")
    public ResponseEntity<ApiResult<DonationItemIdRespDto>> createDonationItem(@AuthenticationPrincipal AuthUser authUser,
                                                                               @RequestBody @Valid DonationItemReqDto request) {
        DonationItemIdRespDto response = donationItemService.createDonationItem(request, authUser);
        return new ResponseEntity<>(ApiResult.success(response), HttpStatus.CREATED);
    }

    @GetMapping("/item/donations")
    public ResponseEntity<ApiResult<DonationItemListRespDto>> getDonationItems(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        DonationItemListRespDto response = donationItemService.getDonationItems(category, search, page, size);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/user/donations")
    public ResponseEntity<ApiResult<DonationItemUserListRespDto>> getUserDonationItems(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        DonationItemUserListRespDto response = donationItemService.getUserDonationItems(authUser, page, size);

        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/users/items/donations/{itemId}")
    public ResponseEntity<ApiResult<DonationItemDetailRespDto>> getDonationItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal AuthUser authUser) {

        DonationItemDetailRespDto response = donationItemService.getDonationItem(itemId, authUser.getId());

        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/donations/{donationItemId}/completion")
    public ResponseEntity<ApiResult<DonationCompleteItemDetailRespDto>> getCompletionDonationItem(
            @PathVariable Long donationItemId,
            @AuthenticationPrincipal AuthUser authUser){

        DonationCompleteItemDetailRespDto response = donationItemService.getCompletionDonationItem(donationItemId, authUser.getId());

        return ResponseEntity.ok(ApiResult.success(response));
    }

}
