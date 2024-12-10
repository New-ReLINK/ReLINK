package com.my.relink.service;

import com.my.relink.controller.like.dto.resp.LikeExchangeItemListRespDto;
import com.my.relink.domain.like.repository.LikeRepository;
import com.my.relink.util.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;

    public PageResponse<LikeExchangeItemListRespDto> getLikeItemList(Long userId, Pageable pageable) {
        return PageResponse.of(likeRepository.findUserLikeExchangeItem(userId, pageable)
                .map(LikeExchangeItemListRespDto::new));
    }
}
