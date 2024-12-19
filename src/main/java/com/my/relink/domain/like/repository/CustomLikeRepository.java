package com.my.relink.domain.like.repository;

import com.my.relink.domain.like.repository.dto.LikeExchangeItemListRepositoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface CustomLikeRepository {

    @Transactional(readOnly = true)
    Page<LikeExchangeItemListRepositoryDto> findUserLikeExchangeItem(Long userId, Pageable pageable);

    Boolean existsLike(Long itemId, Long userId);
}
