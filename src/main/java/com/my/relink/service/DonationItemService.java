package com.my.relink.service;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.donation.dto.PagingInfo;
import com.my.relink.controller.donation.dto.req.DonationItemReqDto;
import com.my.relink.controller.donation.dto.resp.DonationItemListRespDto;
import com.my.relink.controller.donation.dto.resp.DonationItemIdRespDto;
import com.my.relink.controller.donation.dto.resp.DonationItemUserListRespDto;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.category.repository.CategoryRepository;
import com.my.relink.domain.item.donation.DonationItem;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.domain.item.donation.repository.DonationItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DonationItemService {

    private final DonationItemRepository donationItemRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public DonationItemIdRespDto createDonationItem(DonationItemReqDto request, AuthUser authUser) {
        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        DonationItem donationItem = request.toEntity(user, category);

        DonationItem savedItem = donationItemRepository.save(donationItem);

        return new DonationItemIdRespDto(savedItem.getId());
    }

    public DonationItemListRespDto getDonationItems(String category, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DonationItem> donationItems = donationItemRepository.findAllByFilters(category, search, pageable);

        long totalCompletedDonations = donationItemRepository.countCompletedDonations();
        long completedDonationsThisMonth = donationItemRepository.countCompletedDonationsThisMonth();

        return DonationItemListRespDto.of(donationItems, totalCompletedDonations, completedDonationsThisMonth);
    }

    public DonationItemUserListRespDto getUserDonationItems(AuthUser authUser, int page, int size) {
        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);
        Page<DonationItem> donationItems = donationItemRepository.findByUserId(authUser.getId(), pageable);

        PagingInfo pagingInfo = PagingInfo.fromPage(donationItems);

        return DonationItemUserListRespDto.of(donationItems, pagingInfo);
    }

}