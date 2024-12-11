package com.my.relink.service;

import com.my.relink.controller.like.dto.resp.LikeExchangeItemListRespDto;
import com.my.relink.domain.like.repository.LikeRepository;
import com.my.relink.domain.like.repository.dto.LikeExchangeItemListRepositoryDto;
import com.my.relink.util.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;

    public PageResponse<LikeExchangeItemListRespDto> getLikeItemList(Long userId, Pageable pageable) {
        Page<LikeExchangeItemListRepositoryDto> likeExchangeItem
                = likeRepository.findUserLikeExchangeItem(userId, pageable);
        return PageResponse.of(likeExchangeItem.map(LikeExchangeItemListRespDto::new));
    }
}
