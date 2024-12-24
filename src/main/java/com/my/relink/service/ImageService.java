package com.my.relink.service;

import com.my.relink.config.s3.S3Service;
import com.my.relink.controller.image.dto.resp.ImageIdRespDto;
import com.my.relink.controller.image.dto.resp.ImageUserProfileCreateRespDto;
import com.my.relink.controller.image.dto.resp.ImageUserProfileDeleteRespDto;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.repository.ImageRepository;
import com.my.relink.domain.item.donation.DonationItem;
import com.my.relink.domain.item.donation.repository.DonationItemRepository;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private final ImageRepository imageRepository;
    private final DonationItemRepository donationItemRepository;
    private final ExchangeItemRepository exchangeItemRepository;
    private final S3Service s3Service;

    @Transactional
    public void saveImages(List<Image> imageList){
        imageRepository.saveAll(imageList);
    }


    public String getExchangeItemThumbnailUrl(ExchangeItem exchangeItem){
        return imageRepository.findTopByEntityIdAndEntityTypeOrderByCreatedAtAsc(
                        exchangeItem.getId(),
                        EntityType.EXCHANGE_ITEM)
                .map(Image::getImageUrl)
                .orElse(null);
    }

    public Map<Long, List<String>> getImagesByItemIds(EntityType entityType, List<Long> itemIds) {
        List<Image> images = imageRepository.findImages(entityType, itemIds);
        return images.stream()
                .collect(Collectors.toMap(
                        Image::getEntityId,
                        image -> new ArrayList<>(List.of(image.getImageUrl())),
                        (existingList, newList) -> {
                            existingList.addAll(newList);
                            return existingList;
                        }
                ));
    }

    public Map<Long, String> getFirstImagesByItemIds(EntityType entityType, List<Long> itemIds) {
        List<Image> images = imageRepository.findFirstImages(entityType, itemIds);
        return images.stream()
                .collect(Collectors.toMap(Image::getEntityId, Image::getImageUrl));
    }

    @Transactional
    public ImageUserProfileCreateRespDto addUserProfile(Long userId, MultipartFile file) {
        imageRepository.findTopByEntityIdAndEntityTypeOrderByCreatedAtAsc(userId, EntityType.USER)
                .ifPresent(image -> {
                    throw new BusinessException(ErrorCode.ALREADY_IMAGE_FILE);
                });

        String imageUrl = s3Service.upload(file);

        Image image = Image.builder()
                .imageUrl(imageUrl)
                .entityType(EntityType.USER)
                .entityId(userId)
                .build();

        Image savedImage = imageRepository.save(image);
        return new ImageUserProfileCreateRespDto(savedImage);
    }

    @Transactional
    public ImageUserProfileDeleteRespDto deleteUserProfile(Long userId, Long imageId) {
        Image image = imageRepository.findByIdAndEntityIdAndEntityType(imageId, userId, EntityType.USER)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));

        s3Service.deleteImage(image.getImageUrl());
        imageRepository.delete(image);

        return new ImageUserProfileDeleteRespDto(imageId);
    }

    public void deleteImagesByEntityId(EntityType entityType, Long entityId) {
        imageRepository.deleteByEntityTypeAndEntityId(entityType, entityId);
    }

    public String getDonationItemThumbnailUrl(EntityType entityType, Long itemId) {
        return imageRepository.findTopByEntityIdAndEntityTypeOrderByCreatedAtAsc(itemId, entityType)
                .map(Image::getImageUrl)
                .orElse(null);
    }

    public List<String> getImageUrlsByItemId(EntityType entityType, Long itemId) {
        return imageRepository.findImageUrlsByItemId(entityType, itemId);
    }

    @Transactional
    public List<Long> addDonationItemImage(Long userId, Long itemId, List<MultipartFile> files) {
        if (!donationItemRepository.existsByIdAndUserId(itemId, userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        long imageCount = imageRepository.countImages(itemId, EntityType.DONATION_ITEM);

        if (imageCount + files.size() > 5) {
            throw new BusinessException(ErrorCode.MAX_IMAGE_COUNT);
        }

        List<Long> uploadedImageIds = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String imageUrl = s3Service.upload(file);

                Image image = Image.builder()
                        .imageUrl(imageUrl)
                        .entityType(EntityType.DONATION_ITEM)
                        .entityId(itemId)
                        .build();

                Image savedImage = imageRepository.save(image);
                uploadedImageIds.add(savedImage.getId());
            }
        }
        return uploadedImageIds;
    }

    @Transactional
    public List<Long> addExchangeItemImage(Long itemId, List<MultipartFile> files, Long userId) {
        validImageCount(itemId, files);
        validItemOwner(itemId, userId);
        List<Long> savedImageIds = new ArrayList<>();
        for (MultipartFile file : files) {
            String imageUrl = s3Service.upload(file);
            Image image = Image.builder()
                    .imageUrl(imageUrl)
                    .entityType(EntityType.EXCHANGE_ITEM)
                    .entityId(itemId)
                    .build();
            Image savedImage = imageRepository.save(image);
            savedImageIds.add(new ImageUserProfileCreateRespDto(savedImage).getId());
        }
        return savedImageIds;
    }

    @Transactional
    public ImageIdRespDto deleteExchangeItemImage(Long itemId, Long imageId, Long userId) {
        validItemOwner(itemId, userId);
        Image image = imageRepository.findByIdAndEntityIdAndEntityType(imageId, itemId, EntityType.EXCHANGE_ITEM)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));
        s3Service.deleteImage(image.getImageUrl());
        imageRepository.delete(image);
        return new ImageIdRespDto(imageId);
    }

    public void validItemOwner(Long itemId, Long userId) {
        ExchangeItem exchangeItem = exchangeItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));
        if (!exchangeItem.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    public void validImageCount(Long itemId, List<MultipartFile> files) {
        int maxImageCount = 5;
        int existingImageCount = imageRepository.countImages(itemId, EntityType.EXCHANGE_ITEM);
        if (existingImageCount + files.size() > maxImageCount) {
            throw new BusinessException(ErrorCode.MAX_IMAGE_COUNT);
        }
    }

    @Transactional
    public ImageUserProfileDeleteRespDto deleteDonationItemImage(Long userId, Long itemId, Long imageId) {
        validItemOwner(itemId, userId);

        Image image = imageRepository.findByIdAndEntityIdAndEntityType(imageId, itemId, EntityType.DONATION_ITEM)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));

        DonationItem donationItem = donationItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));

        s3Service.deleteImage(image.getImageUrl());

        imageRepository.delete(image);

        return new ImageUserProfileDeleteRespDto(imageId);
    }

}
