package com.my.relink.ExchangeItem;

import com.my.relink.domain.category.Category;
import com.my.relink.domain.category.repository.CategoryRepository;
import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.dto.CreateExchangeItemReqDto;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.user.Role;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.ex.BusinessException;
import com.my.relink.service.ExchangeItemService;
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

    @Test
    void testCreateExchangeItem_Success() {
        MockitoAnnotations.openMocks(this);
        // Given
        CreateExchangeItemReqDto reqDto = CreateExchangeItemReqDto.builder()
                .name("Item Name")
                .description("Description")
                .categoryId(1L)
                .userId(1L)
                .itemQuality(ItemQuality.NEW)
                .deposit(10000)
                .size("M")
                .brand("Brand Name")
                .desiredItem("Desired Item")
                .build();
        Category category = new Category("의류");
        User user = new User("tester", "testUser", "test@mail.com", "1234", "010-1234-5678", Role.USER);
        // When
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(exchangeItemRepository.save(Mockito.any(ExchangeItem.class)))
                .thenAnswer(invocation -> {
                    ExchangeItem savedItem = invocation.getArgument(0);
                    ReflectionTestUtils.setField(savedItem, "id", 1L);
                    System.out.println("Saved ExchangeItem: " + savedItem);
                    return savedItem;
                });
        Long savedId = exchangeItemService.createExchangeItem(reqDto);
        // Then
        assertThat(savedId).isNotNull();
        assertThat(savedId).isEqualTo(1L);
    }
    @Test
    public void testCreateExchangeItem_Fail_DepositLessZero() {
        MockitoAnnotations.openMocks(this);
        // Given
        CreateExchangeItemReqDto reqDto = CreateExchangeItemReqDto.builder()
                .name("Item Name")
                .description("Description")
                .categoryId(1L)
                .userId(1L)
                .itemQuality(ItemQuality.NEW)
                .deposit(-1) // 유효하지 않은 보증금 값
                .size("M")
                .brand("Brand Name")
                .desiredItem("Desired Item")
                .build();
        // Mock 동작 설정
        Category category = new Category("의류");
        User user = new User("tester", "testUser", "test@mail.com", "1234", "010-1234-5678", Role.USER);
        // when
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        //Then
        assertThatThrownBy(() -> exchangeItemService.createExchangeItem(reqDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("보증금은 0원보다 작을 수 없습니다.");
    }
}