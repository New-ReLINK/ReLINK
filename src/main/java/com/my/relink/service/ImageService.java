package com.my.relink.service;

import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.repository.ImageRepository;
import com.my.relink.domain.item.exchange.ExchangeItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

}
