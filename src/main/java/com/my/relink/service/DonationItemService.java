package com.my.relink.service;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.donation.dto.PagingInfo;
import com.my.relink.controller.donation.dto.req.DonationItemReqDto;
import com.my.relink.controller.donation.dto.resp.*;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.category.repository.CategoryRepository;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.item.donation.DonationItem;
import com.my.relink.domain.item.donation.RejectedReason;
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

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DonationItemService {

    private final DonationItemRepository donationItemRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;

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

    public DonationItemDetailRespDto getDonationItem(Long itemId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        DonationItem donationItem = donationItemRepository.findByIdWithCategory(itemId)
                .orElseThrow(()->new BusinessException(ErrorCode.DONATION_ITEM_NOT_FOUND));

        Map<Long, String> imageMap = imageService.getImagesByItemIds(EntityType.DONATION_ITEM, List.of(itemId));

        return DonationItemDetailRespDto.fromEntity(donationItem, imageMap);
    }

    public DonationCompleteItemDetailRespDto getCompletionDonationItem(Long itemId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        DonationItem donationItem = donationItemRepository.findByIdWithCategory(itemId)
                .orElseThrow(()->new BusinessException(ErrorCode.DONATION_ITEM_NOT_FOUND));

        String imageUrl = imageService.getImageByItemId(EntityType.DONATION_ITEM, itemId);
        String certificateUrl = imageService.getImageByItemId(EntityType.DONATION_ITEM, itemId);

        return DonationCompleteItemDetailRespDto.fromEntity(donationItem, imageUrl, certificateUrl);
    }
}