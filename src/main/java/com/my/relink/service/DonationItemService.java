package com.my.relink.service;

import com.my.relink.config.security.AuthUser;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.item.donation.DonationItem;
import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.user.User;
import com.my.relink.controller.item.donation.dto.req.DonationItemReqDto;
import com.my.relink.controller.item.donation.dto.resp.DonationItemRespDto;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.controller.item.donation.repository.CategoryRepository;
import com.my.relink.controller.item.donation.repository.DonationItemRepository;
import com.my.relink.controller.item.donation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DonationItemService {

    private final DonationItemRepository donationItemRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public DonationItemRespDto createDonationItem(DonationItemReqDto request, AuthUser authUser) {
        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        DonationItem donationItem = request.toEntity(user, category);

        DonationItem savedItem = donationItemRepository.save(donationItem);

        return new DonationItemRespDto(savedItem.getId());
    }
}