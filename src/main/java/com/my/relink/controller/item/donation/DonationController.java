package com.my.relink.controller.item.donation;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.item.donation.dto.DonationItemListRespDto;
import com.my.relink.controller.item.donation.dto.DonationItemReqDto;
import com.my.relink.controller.item.donation.dto.DonationItemRespDto;
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
    public ResponseEntity<ApiResult<DonationItemRespDto>> createDonationItem(@AuthenticationPrincipal AuthUser authUser, @RequestBody @Valid DonationItemReqDto request) {
        DonationItemRespDto response = donationItemService.createDonationItem(request, authUser);
        return new ResponseEntity<>(ApiResult.success(response), HttpStatus.CREATED);
    }

    @GetMapping("/item/donation")
    public ResponseEntity<ApiResult<DonationItemListRespDto>> getDonationItems(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        DonationItemListRespDto response = donationItemService.getDonationItems(category, search, page, size);
        return ResponseEntity.ok(ApiResult.success(response));
    }

}