package com.my.relink.service;

import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.like.Like;
import com.my.relink.domain.like.repository.LikeRepository;
import com.my.relink.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {
    @InjectMocks
    private LikeService likeService;

    @Mock
    private LikeRepository likeRepository;
    @Mock
    private ExchangeItemRepository exchangeItemRepository;
    @Mock
    private UserService userService;

    @Test
    @DisplayName("찜하기 성공")
    void testLikeExchangeItem_Success_addLike() {
        Long userId = 1L;
        Long itemId = 100L;
        User user = User.builder()
                .id(userId)
                .build();
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .id(itemId)
                .build();

        when(userService.findByIdOrFail(userId)).thenReturn(user);
        when(exchangeItemRepository.findById(itemId)).thenReturn(Optional.of(exchangeItem));
        when(likeRepository.getLike(itemId, userId)).thenReturn(Optional.empty());
        Like newLike = Like.builder()
                .user(user)
                .exchangeItem(exchangeItem)
                .id(10L)
                .build();
        when(likeRepository.save(any(Like.class))).thenReturn(newLike);

        Long likeId = likeService.toggleLike(userId, itemId);

        assertThat(likeId).isEqualTo(10L);
        verify(likeRepository, times(1)).save(any(Like.class));
        verify(likeRepository, never()).delete(any(Like.class));
    }

    @Test
    @DisplayName("찜 해제하기 성공")
    void testLikeExchangeItem_Success_deleteLike() {
        Long userId = 1L;
        Long itemId = 100L;
        Long likeId = 10L;
        User user = User.builder()
                .id(userId)
                .build();
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .id(itemId)
                .build();
        Like existingLike = Like.builder()
                .id(likeId)
                .user(user)
                .exchangeItem(exchangeItem)
                .build();

        when(userService.findByIdOrFail(userId)).thenReturn(user);
        when(exchangeItemRepository.findById(itemId)).thenReturn(Optional.of(exchangeItem));
        when(likeRepository.getLike(itemId, userId)).thenReturn(Optional.of(existingLike));

        Long deletedLikeId = likeService.toggleLike(userId, itemId);

        assertThat(deletedLikeId).isEqualTo(likeId);
        verify(likeRepository, times(1)).delete(existingLike);
        verify(likeRepository, never()).save(any(Like.class));

    }
}
