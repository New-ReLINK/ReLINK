package com.my.relink.domain.like.repository;

import com.my.relink.domain.like.Like;
import com.my.relink.domain.like.repository.dto.LikeExchangeItemListRepositoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface CustomLikeRepository {

    @Transactional(readOnly = true)
    Page<LikeExchangeItemListRepositoryDto> findUserLikeExchangeItem(Long userId, Pageable pageable);

    Optional<Like> getLike(Long itemId, Long userId);

    Boolean existsLike(Long itemId, Long userId);

    void deleteLike(Long itemId);
}
