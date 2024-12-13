package com.my.relink.service;

import com.my.relink.controller.exchangeItem.dto.CreateExchangeItemReqDto;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.category.repository.CategoryRepository;
import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.repository.PointRepository;
import com.my.relink.domain.user.Role;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.ex.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;


class ExchangeItemServiceTest {
    @InjectMocks
    private ExchangeItemService exchangeItemService;

    @Mock
    private ExchangeItemRepository exchangeItemRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PointRepository pointRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateExchangeItem_Success() {
        // Given
        Long userId = 1L;
        CreateExchangeItemReqDto reqDto = new CreateExchangeItemReqDto(
                "Item Name",
                "Description",
                1L,
                ItemQuality.NEW,
                "M",
                "Brand Name",
                "Desired Item",
                10000
        );
        Category category = new Category("의류");
        User user = new User("tester", "testUser", "test@mail.com", "1234", "010-1234-5678", Role.USER);
        Point point = new Point(20000, user);
        // When
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(pointRepository.findByUserId(userId)).thenReturn(Optional.of(point));
        when(exchangeItemRepository.save(Mockito.any(ExchangeItem.class)))
                .thenAnswer(invocation -> {
                    ExchangeItem savedItem = invocation.getArgument(0);
                    ReflectionTestUtils.setField(savedItem, "id", 1L);
                    System.out.println("Saved ExchangeItem: " + savedItem);
                    return savedItem;
                });
        Long savedId = exchangeItemService.createExchangeItem(reqDto, userId);
        // Then
        assertThat(savedId).isNotNull();
        assertThat(savedId).isEqualTo(1L);
    }

    @Test
    void testCreateExchangeItem_Fail_DepositLessZero() {
        // Given
        Long userId = 1L; // 토큰에서 추출된 사용자 ID
        CreateExchangeItemReqDto reqDto = new CreateExchangeItemReqDto(
                "Item Name",
                "Description",
                1L,
                ItemQuality.NEW,
                "M",
                "Brand Name",
                "Desired Item",
                -1 // 유효하지 않은 보증금 값
        );

        Category category = new Category("의류");
        User user = new User("tester", "testUser", "test@mail.com", "1234", "010-1234-5678", Role.USER);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Then
        assertThatThrownBy(() -> exchangeItemService.createExchangeItem(reqDto, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("보증금은 0원보다 작을 수 없습니다.");
    }

    @Test
    void testCreateExchangeItem_Fail_PointLessThenDeposit() {
        // Given
        Long userId = 1L;
        CreateExchangeItemReqDto reqDto = new CreateExchangeItemReqDto(
                "Item Name",
                "Description",
                1L,
                ItemQuality.NEW,
                "M",
                "Brand Name",
                "Desired Item",
                20000
        );
        Category category = new Category("의류");
        User user = new User("tester", "testUser", "test@mail.com", "1234", "010-1234-5678", Role.USER);
        Point point = new Point(10000, user);
        // when
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(pointRepository.findByUserId(userId)).thenReturn(Optional.of(point));
        // Then
        assertThatThrownBy(() -> exchangeItemService.createExchangeItem(reqDto, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("포인트가 부족합니다");
    }
}