package com.my.relink.ExchangeItem;

import com.my.relink.controller.exchangeItem.dto.req.CreateExchangeItemReqDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetExchangeItemRespDto;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.category.repository.CategoryRepository;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.ImageRepository;
import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.repository.PointRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.trade.repository.TradeRepository;
import com.my.relink.domain.user.Role;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.ex.BusinessException;
import com.my.relink.service.ExchangeItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    @Mock
    private TradeRepository tradeRepository;
    @Mock
    private ImageRepository imageRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("내 교환상품 생성하기 성공")
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
        when(exchangeItemRepository.save(any(ExchangeItem.class)))
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
    @DisplayName("내 교환상품 생성하기 실패 - 보증금 0 미만 입력된 경우")
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
    @DisplayName("내 교환상품 생성하기 실패 - 포인트가 보증금보다 작은 경우")
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

    @Test
    @DisplayName("내 교환상품 목록 불러오기 성공")
    void testGetExchangeItemsByUserId_Success() {
        Long userId1 = 1L;
        Long userId2 = 2L;

        User user1 = User.builder().id(userId1).nickname("User1").build();
        User user2 = User.builder().id(userId2).nickname("User2").build();
        when(userRepository.findById(userId1)).thenReturn(Optional.of(user1));

        ExchangeItem item1 = ExchangeItem.builder().id(1L).name("Item1").user(user1).desiredItem("desiredItem1").tradeStatus(TradeStatus.AVAILABLE).build();
        ExchangeItem item2 = ExchangeItem.builder().id(2L).name("Item2").user(user1).desiredItem("desiredItem2").tradeStatus(TradeStatus.EXCHANGED).build();
        ExchangeItem item3 = ExchangeItem.builder().id(3L).name("Item3").user(user2).desiredItem("desiredItem3").tradeStatus(TradeStatus.EXCHANGED).build();
        List<ExchangeItem> exchangeItems = List.of(item1, item2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<ExchangeItem> page = new PageImpl<>(exchangeItems, pageable, exchangeItems.size());
        when(exchangeItemRepository.findByUserId(userId1, pageable)).thenReturn(page);

        Trade trade1 = Trade.builder()
                .id(1L)
                .ownerExchangeItem(item2)
                .requesterExchangeItem(item3)
                .build();
        trade1.setModifiedAtForTest(LocalDateTime.now()); // Manually set modifiedAt
        when(tradeRepository.findByOwnerExchangeItemIdOrRequesterExchangeItemId(2L, 2L)).thenReturn(Optional.of(trade1));
        when(exchangeItemRepository.findById(3L)).thenReturn(Optional.of(item3));

        when(imageRepository.findFirstImageUrlByEntityTypeAndEntityIdOrderByCreatedAtAsc(EntityType.EXCHANGE_ITEM, 1L))
                .thenReturn(Optional.of("http://example.com/image1.jpg"));
        when(imageRepository.findFirstImageUrlByEntityTypeAndEntityIdOrderByCreatedAtAsc(EntityType.EXCHANGE_ITEM, 2L))
                .thenReturn(Optional.of("http://example.com/image2.jpg"));

        Map<String, Object> result = exchangeItemService.getExchangeItemsByUserId(userId1, 1, 10);
        System.out.println("Result: " + result);

        assertThat(result).isNotNull();
        List<GetExchangeItemRespDto> content = (List<GetExchangeItemRespDto>) result.get("content");
        assertThat(content).hasSize(2);

        GetExchangeItemRespDto item1Dto = content.get(0);
        System.out.println("Item1: " + item1Dto);
        assertThat(item1Dto.getExchangeItemId()).isEqualTo(1L);
        assertThat(item1Dto.getImageUrl()).isEqualTo("http://example.com/image1.jpg");
        assertThat(item1Dto.getTradeStatus()).isEqualTo(TradeStatus.AVAILABLE);

        GetExchangeItemRespDto item2Dto = content.get(1);
        System.out.println("Item2: " + item2Dto);
        assertThat(item2Dto.getExchangeItemId()).isEqualTo(2L);
        assertThat(item2Dto.getImageUrl()).isEqualTo("http://example.com/image2.jpg");
        assertThat(item2Dto.getTradeStatus()).isEqualTo(TradeStatus.EXCHANGED);
        assertThat(item2Dto.getTradePartnerNickname()).isEqualTo("User2");
    }

    @Test
    @DisplayName("내 교환상품 목록 불러오기 성공 - 목록이 없을 경우 빈 목록 불러오기")
    void testGetExchangeItemByUserId_Success_NoItem() {
        Long userId = 1L;
        User user = User.builder().id(userId).nickname("User1").build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<ExchangeItem> emptyPage = Page.empty(pageable);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(exchangeItemRepository.findByUserId(userId, pageable)).thenReturn(emptyPage);

        Map<String, Object> result = exchangeItemService.getExchangeItemsByUserId(userId, 1, 10);
        assertThat(result).isNotNull();
        System.out.println("Result: " + result);

        List<GetExchangeItemRespDto> content = (List<GetExchangeItemRespDto>) result.get("content");
        assertThat(content).isEmpty();

        Map<String, Object> pageInfo = (Map<String, Object>) result.get("pageInfo");
        assertThat(pageInfo.get("totalElements")).isEqualTo(0);
        assertThat(pageInfo.get("totalPages")).isEqualTo(0);
        assertThat(pageInfo.get("hasPrevious")).isEqualTo(false);
        assertThat(pageInfo.get("hasNext")).isEqualTo(false);
    }
}