package com.my.relink.service;

import com.my.relink.controller.like.dto.resp.LikeExchangeItemListRespDto;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.like.Like;
import com.my.relink.domain.like.repository.LikeRepository;
import com.my.relink.domain.like.repository.dto.LikeExchangeItemListRepositoryDto;
import com.my.relink.domain.user.User;
import com.my.relink.util.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final ExchangeItemService exchangeItemService;
    private final UserService userService;

    public PageResponse<LikeExchangeItemListRespDto> getLikeItemList(Long userId, Pageable pageable) {
        Page<LikeExchangeItemListRepositoryDto> likeExchangeItem
                = likeRepository.findUserLikeExchangeItem(userId, pageable);
        return PageResponse.of(likeExchangeItem.map(LikeExchangeItemListRespDto::new));
    }

    @Transactional
    public Long toggleLike(Long userId, Long itemId) {
        User user = userService.findByIdOrFail(userId);
        ExchangeItem exchangeItem = exchangeItemService.findByIdOrFail(itemId);

        return likeRepository.findByUserAndExchangeItem(user, exchangeItem)
                .map(like -> {
                    Long likeId = like.getId();
                    likeRepository.delete(like);
                    return likeId;
                })
                .orElseGet(() -> {
                    Like savedLike = likeRepository.save(
                            Like.builder()
                                    .user(user)
                                    .exchangeItem(exchangeItem)
                                    .build()
                    );
                    return savedLike.getId();
                });
    }
}
