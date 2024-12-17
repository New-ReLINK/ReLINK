package com.my.relink.service;

import com.my.relink.config.s3.S3Service;
import com.my.relink.controller.image.dto.resp.ImageUserProfileCreateRespDto;
import com.my.relink.controller.image.dto.resp.ImageUserProfileDeleteRespDto;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.repository.ImageRepository;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private final ImageRepository imageRepository;
    private final S3Service s3Service;

    public String getExchangeItemUrl(ExchangeItem exchangeItem) {
        return imageRepository.findTopByEntityIdAndEntityTypeOrderByCreatedAtAsc(
                        exchangeItem.getId(),
                        EntityType.EXCHANGE_ITEM)
                .map(Image::getImageUrl)
                .orElse(null);
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
}
