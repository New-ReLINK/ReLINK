package com.my.relink.controller.item.donation;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.item.donation.dto.req.DonationItemReqDto;
import com.my.relink.controller.item.donation.dto.resp.DonationItemRespDto;
import com.my.relink.service.DonationItemService;
import com.my.relink.util.api.ApiResult;
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
    public ResponseEntity<ApiResult<DonationItemRespDto>> createDonationItem(@AuthenticationPrincipal AuthUser authUser, @RequestBody DonationItemReqDto request) {
        DonationItemRespDto response = donationItemService.createDonationItem(request, authUser);
        return new ResponseEntity<>(ApiResult.success(response), HttpStatus.CREATED);
    }

}
