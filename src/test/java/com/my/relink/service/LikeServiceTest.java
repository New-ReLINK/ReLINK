package com.my.relink.service;

import com.my.relink.domain.item.exchange.ExchangeItem;
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
    private ExchangeItemService exchangeItemService;

    @Test
    @DisplayName("찜하기 성공")
    void testLikeExchangeItem_Success_addLike() {
        Long userId = 1L;
        Long itemId = 100L;
        User user = mock(User.class);
        ExchangeItem exchangeItem = mock(ExchangeItem.class);

        when(exchangeItemService.getValidUser(userId)).thenReturn(user);
        when(exchangeItemService.findByIdOrFail(itemId)).thenReturn(exchangeItem);
        when(likeRepository.findByUserAndExchangeItem(user, exchangeItem)).thenReturn(Optional.empty());
        Like newLike = Like.builder()
                .user(user)
                .exchangeItem(exchangeItem)
                .id(10L)
                .build();
        when(likeRepository.save(any(Like.class))).thenReturn(newLike);

        Long likeId = likeService.toggleLike(userId, itemId);

        assertThat(likeId).isEqualTo(10L);
        verify(likeRepository, times(1)).save(any(Like.class));
    }

    @Test
    @DisplayName("찜 해제하기 성공")
    void testLikeExchangeItem_Success_deleteLike() {
        Long userId = 1L;
        Long itemId = 100L;
        Long likeId = 10L;
        User user = mock(User.class);
        ExchangeItem exchangeItem = mock(ExchangeItem.class);
        Like existingLike = Like.builder()
                .id(likeId)
                .user(user)
                .exchangeItem(exchangeItem)
                .build();

        when(exchangeItemService.getValidUser(userId)).thenReturn(user);
        when(exchangeItemService.findByIdOrFail(itemId)).thenReturn(exchangeItem);
        when(likeRepository.findByUserAndExchangeItem(user, exchangeItem)).thenReturn(Optional.of(existingLike));

        Long deletedLikeId = likeService.toggleLike(userId, itemId);

        assertThat(deletedLikeId).isEqualTo(likeId);
        verify(likeRepository, times(1)).deleteByUserAndExchangeItem(user, exchangeItem);
        verify(likeRepository, never()).save(any(Like.class));

    }
}
