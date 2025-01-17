package com.my.relink.service;

import com.my.relink.common.notification.NotificationPublisherService;
import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.donation.dto.PagingInfo;
import com.my.relink.controller.donation.dto.req.DonationItemRejectReqDto;
import com.my.relink.controller.donation.dto.req.DonationItemReqDto;
import com.my.relink.controller.donation.dto.resp.*;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.category.repository.CategoryRepository;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.item.donation.*;
import com.my.relink.domain.item.donation.RejectedReason;
import com.my.relink.domain.user.Address;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.domain.item.donation.repository.DonationItemRepository;
import com.my.relink.util.MetricConstants;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Timed(MetricConstants.SERVICE_DONATION_ITEM_TIME)
public class DonationItemService {

    private final DonationItemRepository donationItemRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final NotificationPublisherService notificationPublisherService;

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
        long completedDonationsThisMonth = countCompletedDonationsThisMonth();

        return DonationItemListRespDto.of(donationItems, totalCompletedDonations, completedDonationsThisMonth);
    }

    public long countCompletedDonationsThisMonth() {
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        return donationItemRepository.countCompletedDonationsThisMonth(currentYear, currentMonth);
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

        Map<Long, List<String>> imageMap = imageService.getImagesByItemIds(EntityType.DONATION_ITEM, List.of(itemId));

        return DonationItemDetailRespDto.fromEntity(donationItem, imageMap);
    }

    public DonationItemRejectionRespDto getRejectionItem(Long itemId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        DonationItem donationItem = donationItemRepository.findByIdWithCategory(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DONATION_ITEM_NOT_FOUND));

        String imageUrl = imageService.getDonationItemThumbnailUrl(EntityType.DONATION_ITEM, itemId);

        RejectedReason rejectedReason = donationItem.getRejectedReason();

        notificationPublisherService.createDonationNotification(
                userId,
                donationItem.getName(),
                DonationStatus.INSPECTION_REJECTED
        );

        return DonationItemRejectionRespDto.fromEntity(donationItem, imageUrl, rejectedReason);
    }

    public DonationCompleteItemDetailRespDto getCompletionDonationItem(Long itemId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        DonationItem donationItem = donationItemRepository.findByIdWithCategory(itemId)
                .orElseThrow(()->new BusinessException(ErrorCode.DONATION_ITEM_NOT_FOUND));

        String imageUrl = imageService.getDonationItemThumbnailUrl(EntityType.DONATION_ITEM, itemId);
        String certificateUrl = imageService.getDonationItemThumbnailUrl(EntityType.DONATION_CERTIFICATION, itemId);

        notificationPublisherService.createDonationNotification(
                userId,
                donationItem.getName(),
                DonationStatus.DONATION_COMPLETED
        );

        return DonationCompleteItemDetailRespDto.fromEntity(donationItem, imageUrl, certificateUrl);
    }

    @Transactional
    public DonationItemIdRespDto deleteDonationItem(Long itemId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        DonationItem donationItem = donationItemRepository.findByIdWithCategory(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DONATION_ITEM_NOT_FOUND));

        if (!donationItem.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        if (donationItem.getDonationStatus() == DonationStatus.UNDER_INSPECTION ||
                donationItem.getDonationStatus() == DonationStatus.INSPECTION_COMPLETED ||
                donationItem.getDonationStatus() == DonationStatus.DONATION_COMPLETED) {
            throw new BusinessException(ErrorCode.DONATION_ITEM_CANNOT_BE_DELETED);
        }

        imageService.deleteImages(EntityType.DONATION_ITEM, itemId);

        donationItemRepository.delete(donationItem);

        return new DonationItemIdRespDto(itemId);
    }

    public DonationItemIdRespDto rejectDonationItemDisposal(Long itemId, Long userId, DonationItemRejectReqDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        DonationItem donationItem = donationItemRepository.findByIdWithCategory(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DONATION_ITEM_NOT_FOUND));

        if (request.getDisposal() == DisposalType.RETURNED) {
            Address address = new Address(request.getZipcode(), request.getBaseAddress(), request.getDetailAddress());
            donationItem.updateReturnAddress(address);
        }

        donationItem.updateStatus(request.getDisposal());

        DonationItem savedItem = donationItemRepository.save(donationItem);

        notificationPublisherService.createDonationNotification(
                userId,
                donationItem.getName(),
                DonationStatus.INSPECTION_REJECTED
        );

        return new DonationItemIdRespDto(savedItem.getId());
    }

}
