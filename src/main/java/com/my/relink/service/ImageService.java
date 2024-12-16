package com.my.relink.service;

import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.repository.ImageRepository;
import com.my.relink.domain.item.exchange.ExchangeItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private final ImageRepository imageRepository;

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

}
