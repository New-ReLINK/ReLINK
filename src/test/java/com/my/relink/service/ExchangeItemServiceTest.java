package com.my.relink.service;


import com.my.relink.chat.service.ChatService;
import com.my.relink.controller.exchangeItem.dto.req.CreateExchangeItemReqDto;
import com.my.relink.controller.exchangeItem.dto.req.GetAllExchangeItemReqDto;
import com.my.relink.controller.exchangeItem.dto.req.UpdateExchangeItemReqDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetAllExchangeItemsRespDto;
import com.my.relink.controller.exchangeItem.dto.resp.GetExchangeItemRespDto;
import com.my.relink.domain.BaseEntity;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.category.repository.CategoryRepository;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.repository.PointRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.Role;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.util.page.PageInfo;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    private TradeService tradeService;
    @Mock
    private ImageService imageService;
    @Mock
    private LikeService likeService;
    @Mock
    private ChatService chatService;
    @Mock
    private UserTrustScoreService userTrustScoreService;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("내 교환상품 생성하기 성공")
    void testCreateExchangeItem_Success() {
        Long userId = 1L;
        CreateExchangeItemReqDto reqDto = new CreateExchangeItemReqDto(
                "Item Name",
                "Description",
                1L,
                ItemQuality.NEW,
                "M",
                "Brand Name",
                "Desired Item",
                10000,
                false
        );
        Category category = new Category("의류");
        User user = new User("tester", "testUser", "test@mail.com", "1234", "010-1234-5678", Role.USER);
        Point point = new Point(20000, user);
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
        assertThat(savedId).isNotNull();
        assertThat(savedId).isEqualTo(1L);
    }

    @Test
    @DisplayName("내 교환상품 생성하기 실패 - 보증금 0 미만 입력된 경우")
    void testCreateExchangeItem_Fail_DepositLessZero() {
        Long userId = 1L;
        CreateExchangeItemReqDto reqDto = new CreateExchangeItemReqDto(
                "Item Name",
                "Description",
                1L,
                ItemQuality.NEW,
                "M",
                "Brand Name",
                "Desired Item",
                -1, // 유효하지 않은 보증금 값
                false
        );
        Category category = new Category("의류");
        User user = new User("tester", "testUser", "test@mail.com", "1234", "010-1234-5678", Role.USER);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> exchangeItemService.createExchangeItem(reqDto, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("보증금은 0원보다 작을 수 없습니다.");
    }

    @Test
    @DisplayName("내 교환상품 생성하기 실패 - 포인트가 보증금보다 작은 경우")
    void testCreateExchangeItem_Fail_PointLessThenDeposit() {
        Long userId = 1L;
        CreateExchangeItemReqDto reqDto = new CreateExchangeItemReqDto(
                "Item Name",
                "Description",
                1L,
                ItemQuality.NEW,
                "M",
                "Brand Name",
                "Desired Item",
                20000,
                false
        );
        Category category = new Category("의류");
        User user = new User("tester", "testUser", "test@mail.com", "1234", "010-1234-5678", Role.USER);
        Point point = new Point(10000, user);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(pointRepository.findByUserId(userId)).thenReturn(Optional.of(point));
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
                .tradeStatus(TradeStatus.EXCHANGED)
                .build();
        try {
            Field modifiedAtField = BaseEntity.class.getDeclaredField("modifiedAt");
            modifiedAtField.setAccessible(true);
            modifiedAtField.set(trade1, LocalDateTime.of(2023, 12, 1, 12, 0));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Reflection failed", e);
        }
        when(tradeService.getTradesByItemIds(List.of(1L, 2L))).thenReturn(Map.of(2L, trade1));
        Map<Long, String> imageMap = Map.of(
                1L, "http://example.com/image1.jpg",
                2L, "http://example.com/image2.jpg"
        );
        when(imageService.getFirstImagesByItemIds(EntityType.EXCHANGE_ITEM, List.of(1L, 2L)))
                .thenReturn(imageMap);
        GetExchangeItemRespDto result = exchangeItemService.getExchangeItemsByUserId(userId1, 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isInstanceOf(List.class);
        assertThat(result.getPageInfo()).isNotNull();

        List<GetExchangeItemRespDto> content = result.getContent();
        assertThat(content).hasSize(2);

        GetExchangeItemRespDto item1Dto = content.get(0);
        assertThat(item1Dto.getExchangeItemId()).isEqualTo(1L);
        assertThat(item1Dto.getExchangeItemName()).isEqualTo("Item1");
        assertThat(item1Dto.getImageUrl()).isEqualTo("http://example.com/image1.jpg");
        assertThat(item1Dto.getTradeStatus()).isEqualTo(TradeStatus.AVAILABLE);
        assertThat(item1Dto.getDesiredItem()).isEqualTo("desiredItem1");
        assertThat(item1Dto.getSize()).isNull();
        assertThat(item1Dto.getTradePartnerNickname()).isNull();
        assertThat(item1Dto.getCompletedDate()).isNull();

        GetExchangeItemRespDto item2Dto = content.get(1);
        assertThat(item2Dto.getExchangeItemId()).isEqualTo(2L);
        assertThat(item2Dto.getExchangeItemName()).isEqualTo("Item2");
        assertThat(item2Dto.getImageUrl()).isEqualTo("http://example.com/image2.jpg");
        assertThat(item2Dto.getTradeStatus()).isEqualTo(TradeStatus.EXCHANGED);
        assertThat(item2Dto.getDesiredItem()).isNull();
        assertThat(item2Dto.getTradePartnerNickname()).isEqualTo("User2");
        assertThat(item2Dto.getCompletedDate()).isNotNull();
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

        GetExchangeItemRespDto result = exchangeItemService.getExchangeItemsByUserId(userId, 1, 10);
        assertThat(result).isNotNull();
        System.out.println("Result: " + result);

        List<GetExchangeItemRespDto> content = result.getContent();
        assertThat(content).isEmpty();

        PageInfo pageInfo = result.getPageInfo();
        assertThat(pageInfo.getTotalCount()).isEqualTo(0);
        assertThat(pageInfo.getTotalPages()).isEqualTo(0);
        assertThat(pageInfo.isHasPrevious()).isEqualTo(false);
        assertThat(pageInfo.isHasNext()).isEqualTo(false);
    }

    @Test
    @DisplayName("내 교환 상품을 수정하기 위한 페이지 조회 성공")
    void testGetExchangeItemModifyPage_Success() {
        Long userId = 1L;
        Long itemId = 2L;
        Category category = new Category("의류");
        User user = User.builder().id(userId).build();
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .id(itemId)
                .name("Test Item")
                .description("Test Description")
                .category(category)
                .itemQuality(ItemQuality.NEW)
                .user(user)
                .deposit(5000)
                .size("M")
                .brand("Test Brand")
                .desiredItem("Test Desired Item")
                .tradeStatus(TradeStatus.AVAILABLE)
                .isDeleted(false)
                .build();
        Mockito.when(exchangeItemRepository.findById(itemId)).thenReturn(Optional.of(exchangeItem));
        GetExchangeItemRespDto result = exchangeItemService.getExchangeItemModifyPage(itemId, userId);

        Assertions.assertNotNull(result);
        assertEquals("Test Item", result.getExchangeItemName());
        assertEquals("Test Description", result.getDescription());
        assertEquals("의류", result.getCategory().getName());
        assertEquals(ItemQuality.NEW, result.getItemQuality());
        assertEquals("M", result.getSize());
        assertEquals("Test Brand", result.getBrand());
        assertEquals("Test Desired Item", result.getDesiredItem());
    }

    @Test
    @DisplayName("내 교환 상품을 수정하기 위한 페이지 조회 실패 - 해당 상품의 소유자가 아닌 경우")
    void testGetExchangeItemModifyPage_Fail_NotOwner() {
        Long userId = 1L;
        Long itemId = 2L;
        Category category = new Category("의류");
        User user = User.builder().id(2L).build();
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .id(itemId)
                .name("Test Item")
                .description("Test Description")
                .category(category)
                .itemQuality(ItemQuality.NEW)
                .user(user) // 상품 등록 유저의 id는 2
                .deposit(5000)
                .size("M")
                .brand("Test Brand")
                .desiredItem("Test Desired Item")
                .tradeStatus(TradeStatus.AVAILABLE)
                .isDeleted(false)
                .build();
        Mockito.when(exchangeItemRepository.findById(itemId)).thenReturn(Optional.of(exchangeItem));
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            exchangeItemService.getExchangeItemModifyPage(itemId, userId);
        });
        assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());
    }

    @Test
    @DisplayName("내 교환 상품을 수정하기 위한 페이지 조회 실패 - 해당 상품이 없는 경우")
    void testGetExchangeItemModifyPage_Fail_NoItem() {
        Long userId = 1L;
        Long itemId = 2L;
        Mockito.when(exchangeItemRepository.findById(itemId)).thenReturn(Optional.empty());
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            exchangeItemService.getExchangeItemModifyPage(itemId, userId);
        });
        assertEquals(ErrorCode.EXCHANGE_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("내 교환 상품 수정하기 성공")
    void testUpdateExchangeItem_Success() {
        Long userId = 1L;
        Long itemId = 2L;
        Category category = new Category("의류");
        User user = User.builder().id(1L).build();
        Point point = new Point(20000, user);
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .id(itemId)
                .name("Test Item")
                .description("Test Description")
                .category(category)
                .itemQuality(ItemQuality.NEW)
                .user(user)
                .deposit(5000)
                .size("M")
                .brand("Test Brand")
                .desiredItem("Test Desired Item")
                .tradeStatus(TradeStatus.AVAILABLE)
                .isDeleted(false)
                .build();
        UpdateExchangeItemReqDto reqDto = new UpdateExchangeItemReqDto(
                "New Item Name",
                "New Description",
                1L,
                ItemQuality.NEW,
                "M",
                "New Brand",
                "New Desired Item",
                10000
        );
        when(exchangeItemRepository.findById(itemId)).thenReturn(Optional.of(exchangeItem));
        when(categoryRepository.findById(reqDto.getCategoryId())).thenReturn(Optional.of(category));
        when(pointRepository.findByUserId(userId)).thenReturn(Optional.of(point));

        Long updatedItemId = exchangeItemService.updateExchangeItem(itemId, reqDto, userId);

        assertThat(updatedItemId).isEqualTo(itemId);
        assertThat(exchangeItem.getName()).isEqualTo(reqDto.getName());
        assertThat(exchangeItem.getDescription()).isEqualTo(reqDto.getDescription());
        assertThat(exchangeItem.getCategory()).isEqualTo(category);
        assertThat(exchangeItem.getItemQuality()).isEqualTo(reqDto.getItemQuality());
        assertThat(exchangeItem.getSize()).isEqualTo(reqDto.getSize());
        assertThat(exchangeItem.getBrand()).isEqualTo(reqDto.getBrand());
        assertThat(exchangeItem.getDesiredItem()).isEqualTo(reqDto.getDesiredItem());
        assertThat(exchangeItem.getDeposit()).isEqualTo(reqDto.getDeposit());


    }

    @Test
    @DisplayName("내 교환 상품 수정하기 실패 - 보증금 0 미만")
    void testUpdateExchangeItem_Fail_DepositLessZero() {
        Long userId = 1L;
        Long itemId = 2L;
        Category category = new Category("의류");
        User user = User.builder().id(1L).build();
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .id(itemId)
                .name("Test Item")
                .description("Test Description")
                .category(category)
                .itemQuality(ItemQuality.NEW)
                .user(user)
                .deposit(5000)
                .size("M")
                .brand("Test Brand")
                .desiredItem("Test Desired Item")
                .tradeStatus(TradeStatus.AVAILABLE)
                .isDeleted(false)
                .build();
        UpdateExchangeItemReqDto reqDto = new UpdateExchangeItemReqDto(
                "New Item Name",
                "New Description",
                1L,
                ItemQuality.NEW,
                "M",
                "New Brand",
                "New Desired Item",
                -1 // 보증금이 0 미만

        );
        when(exchangeItemRepository.findById(itemId)).thenReturn(Optional.of(exchangeItem));
        when(categoryRepository.findById(reqDto.getCategoryId())).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> exchangeItemService.updateExchangeItem(itemId, reqDto, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("보증금은 0원보다 작을 수 없습니다.");
    }

    @Test
    @DisplayName("내 교환 상품 수정하기 실패 - 포인트가 보증금보다 작은 경우")
    void testUpdateExchangeItem_Fail_PointLessThenDeposit() {
        Long userId = 1L;
        Long itemId = 2L;
        Category category = new Category("의류");
        User user = User.builder().id(1L).build();
        Point point = new Point(5000, user);
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .id(itemId)
                .name("Test Item")
                .description("Test Description")
                .category(category)
                .itemQuality(ItemQuality.NEW)
                .user(user)
                .deposit(5000)
                .size("M")
                .brand("Test Brand")
                .desiredItem("Test Desired Item")
                .tradeStatus(TradeStatus.AVAILABLE)
                .isDeleted(false)
                .build();
        UpdateExchangeItemReqDto reqDto = new UpdateExchangeItemReqDto(
                "New Item Name",
                "New Description",
                1L,
                ItemQuality.NEW,
                "M",
                "New Brand",
                "New Desired Item",
                10000 // 요구 보증금이 보유 포인트보다 큼
        );
        when(exchangeItemRepository.findById(itemId)).thenReturn(Optional.of(exchangeItem));
        when(categoryRepository.findById(reqDto.getCategoryId())).thenReturn(Optional.of(category));
        when(pointRepository.findByUserId(userId)).thenReturn(Optional.of(point));

        assertThatThrownBy(() -> exchangeItemService.updateExchangeItem(itemId, reqDto, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("포인트가 부족합니다");
    }

    @Test
    @DisplayName("내 교환 상품 삭제하기 성공")
    void testDeleteExchangeItem_Success() {
        Long userId = 1L;
        Long itemId = 100L;
        Long tradeId = 200L;
        User user = User.builder().id(userId).build();
        ExchangeItem exchangeItem = mock(ExchangeItem.class);

        when(exchangeItemRepository.findById(itemId)).thenReturn(Optional.of(exchangeItem));
        when(exchangeItem.getUser()).thenReturn(user);
        when(exchangeItem.getId()).thenReturn(itemId);
        when(tradeService.getTradeIdByItemId(itemId)).thenReturn(tradeId);

        Long deletedItemId = exchangeItemService.deleteExchangeItem(itemId, userId);

        assertThat(deletedItemId).isEqualTo(itemId);
        verify(exchangeItem).validExchangeItemOwner(userId, userId);
        verify(exchangeItem).delete();
        verify(imageService, times(1)).deleteImagesByEntityId(EntityType.EXCHANGE_ITEM, itemId);
        verify(likeService, times(1)).deleteLikesByExchangeItemId(itemId);
        verify(chatService, times(1)).deleteChatsByTradeId(tradeId);
    }

    @Test
    @DisplayName("내 교환 상품 삭제하기 실패 - 상품의 소유자가 아닌 경우 ")
    void testDeleteExchangeItem_Fail_NotOwner() {
        Long userId = 1L;
        Long invalidUserId = 999L; // 잘못된 사용자 ID
        Long itemId = 100L;
        User ownerUser = User.builder().id(userId).build();
        ExchangeItem exchangeItem = mock(ExchangeItem.class);

        when(exchangeItemRepository.findById(itemId)).thenReturn(Optional.of(exchangeItem));
        when(exchangeItem.getUser()).thenReturn(ownerUser);
        doThrow(new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS))
                .when(exchangeItem).validExchangeItemOwner(userId, invalidUserId);
        BusinessException exception = assertThrows(BusinessException.class, () ->
                exchangeItemService.deleteExchangeItem(itemId, invalidUserId));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_ACCESS);
        verify(exchangeItem).validExchangeItemOwner(userId, invalidUserId);
        verify(exchangeItem, never()).delete();
        verify(imageService, never()).deleteImagesByEntityId(any(), anyLong());
        verify(likeService, never()).deleteLikesByExchangeItemId(anyLong());
        verify(chatService, never()).deleteChatsByTradeId(anyLong());
    }

    @Test
    @DisplayName("내 교환 상품 삭제하기 실패 - 상품의 IN_EXCHANGE인 경우")
    void testDeleteExchangeItem_Fail_InExchange() {
        Long userId = 1L;
        Long itemId = 100L;
        ExchangeItem exchangeItem = mock(ExchangeItem.class);
        User user = User.builder().id(userId).build();

        when(exchangeItemRepository.findById(itemId)).thenReturn(Optional.of(exchangeItem));
        when(exchangeItem.getTradeStatus()).thenReturn(TradeStatus.IN_EXCHANGE); // 거래 상태가 IN_EXCHANGE
        when(exchangeItem.getUser()).thenReturn(user);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> exchangeItemService.deleteExchangeItem(itemId, userId));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ITEM_IN_EXCHANGE);
        verify(exchangeItem, never()).delete();
        verify(imageService, never()).deleteImagesByEntityId(any(), anyLong());
        verify(likeService, never()).deleteLikesByExchangeItemId(anyLong());
        verify(chatService, never()).deleteChatsByTradeId(anyLong());
    }

    @Test
    @DisplayName("교환상품 전체목록 조회 성공")
    void testGetAllExchangeItems_Success() {
        GetAllExchangeItemReqDto reqDto = GetAllExchangeItemReqDto.builder()
                .search("shoes")
                .deposit("desc")
                .tradeStatus(TradeStatus.AVAILABLE)
                .categoryId(1L)
                .page(0)
                .size(10)
                .build();
        Category category = mock(Category.class);
        ExchangeItem exchangeItem = mock(ExchangeItem.class);
        Page<ExchangeItem> exchangeItemsPage = new PageImpl<>(List.of(exchangeItem));

        when(categoryRepository.findById(reqDto.getCategoryId())).thenReturn(Optional.of(category));
        when(exchangeItemRepository.findAllByCriteria(
                reqDto.getSearch(),
                reqDto.getTradeStatus(),
                category,
                reqDto.getDeposit(),
                PageRequest.of(reqDto.getPage(), reqDto.getSize())
        )).thenReturn(exchangeItemsPage);
        when(exchangeItem.getId()).thenReturn(100L);
        when(exchangeItem.getName()).thenReturn("Nike Air Max");
        when(exchangeItem.getUser()).thenReturn(User.builder().id(1L).nickname("JohnDoe").build());
        when(imageService.getFirstImagesByItemIds(any(), any()))
                .thenReturn(Map.of(100L, "http://example.com/image1.jpg"));
        when(userTrustScoreService.getTrustScore(any())).thenReturn(85);

        GetAllExchangeItemsRespDto result = exchangeItemService.getAllExchangeItems(reqDto);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getExchangeItemId()).isEqualTo(100L);
        assertThat(result.getContent().get(0).getExchangeItemName()).isEqualTo("Nike Air Max");
        assertThat(result.getContent().get(0).getImageUrl()).isEqualTo("http://example.com/image1.jpg");
        assertThat(result.getContent().get(0).getOwnerNickname()).isEqualTo("JohnDoe");
        assertThat(result.getContent().get(0).getOwnerTrustScore()).isEqualTo(85);

        verify(categoryRepository, times(1)).findById(reqDto.getCategoryId());
        verify(exchangeItemRepository, times(1)).findAllByCriteria(
                reqDto.getSearch(),
                reqDto.getTradeStatus(),
                category,
                reqDto.getDeposit(),
                PageRequest.of(reqDto.getPage(), reqDto.getSize())
        );
        verify(imageService, times(1)).getFirstImagesByItemIds(any(), any());
        verify(userTrustScoreService, times(1)).getTrustScore(any());
    }

    @Test
    @DisplayName("교환상품 전체목록 조회 실패 - 보증금 기준 정렬 옵션에 해당되지 않은 값이 들어온 경우")
    void testGetAllExchangeItems_Fail_INVALID_SORT_PARAMETER() {
        GetAllExchangeItemReqDto dto = GetAllExchangeItemReqDto.builder()
                .deposit("aasc")
                .build();

        Set<ConstraintViolation<GetAllExchangeItemReqDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("보증금 정렬 기준 값이 올바르지 않습니다.");
    }

    @Test
    @DisplayName("교환하기 페이지 조회 성공")
    void testGetExchangeItemFromOwner_Success() {
        Long itemId = 1L;
        Long userId = 2L;
        User user = User.builder().id(1L).nickname("Test User").build();
        Category category = new Category("의류");
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .id(itemId)
                .name("Test Item")
                .description("Test Description")
                .category(category)
                .itemQuality(ItemQuality.NEW)
                .user(user)
                .deposit(5000)
                .size("M")
                .brand("Test Brand")
                .desiredItem("Test Desired Item")
                .tradeStatus(TradeStatus.AVAILABLE)
                .isDeleted(false)
                .build();
        try {
            Field createdAtField = BaseEntity.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(exchangeItem, LocalDateTime.of(2023, 12, 1, 12, 0));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Reflection failed", e);
        }

        when(exchangeItemRepository.findById(itemId)).thenReturn(Optional.of(exchangeItem));
        when(userTrustScoreService.getTrustScore(any())).thenReturn(90);
        when(imageService.getImageUrlsByItemId(any(), any())).thenReturn(Arrays.asList("img1.jpg", "img2.jpg"));
        when(likeService.existsItemLike(itemId, userId)).thenReturn(true);

        GetAllExchangeItemsRespDto result = exchangeItemService.getExchangeItemFromOwner(itemId, userId);

        assertEquals(exchangeItem.getId(), result.getExchangeItemId());
        assertEquals("Test Item", result.getExchangeItemName());
        assertEquals("Test Description", result.getDescription());
        assertEquals("의류", result.getCategory());
        assertEquals(5000, result.getDeposit());
        assertEquals(LocalDate.of(2023, 12, 1), result.getCreatedAt());
        assertEquals("교환 가능", result.getTradeStatus());
        assertEquals(ItemQuality.NEW, result.getItemQuality());
        assertEquals("Test Desired Item", result.getDesiredItem());
        assertEquals(Arrays.asList("img1.jpg", "img2.jpg"), result.getImageUrls());
        assertEquals(user.getId(), result.getOwnerId());
        assertEquals("Test User", result.getOwnerNickname());
        assertEquals(90, result.getOwnerTrustScore());
        assertTrue(result.getLike());
    }

    @Test
    @DisplayName("교환하기 페이지 조회 성공 - 이미지가 없는 경우")
    void testGetExchangeItemFromOwner_Success_NotImage () {
        Long itemId = 1L;
        Long userId = 2L;
        User user = User.builder().id(1L).nickname("Test User").build();
        Category category = new Category("의류");
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .id(itemId)
                .name("Test Item")
                .description("Test Description")
                .category(category)
                .itemQuality(ItemQuality.NEW)
                .user(user)
                .deposit(5000)
                .size("M")
                .brand("Test Brand")
                .desiredItem("Test Desired Item")
                .tradeStatus(TradeStatus.AVAILABLE)
                .isDeleted(false)
                .build();
        try {
            Field createdAtField = BaseEntity.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(exchangeItem, LocalDateTime.of(2023, 12, 1, 12, 0));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Reflection failed", e);
        }

        when(exchangeItemRepository.findById(itemId)).thenReturn(Optional.of(exchangeItem));
        when(userTrustScoreService.getTrustScore(any())).thenReturn(90);
        when(imageService.getImageUrlsByItemId(any(), any())).thenReturn(Collections.emptyList());
        when(likeService.existsItemLike(itemId, userId)).thenReturn(true);

        GetAllExchangeItemsRespDto result = exchangeItemService.getExchangeItemFromOwner(itemId, userId);

        assertEquals(exchangeItem.getId(), result.getExchangeItemId());
        assertEquals("Test Item", result.getExchangeItemName());
        assertEquals("Test Description", result.getDescription());
        assertEquals("의류", result.getCategory());
        assertEquals(5000, result.getDeposit());
        assertEquals(LocalDate.of(2023, 12, 1), result.getCreatedAt());
        assertEquals("교환 가능", result.getTradeStatus());
        assertEquals(ItemQuality.NEW, result.getItemQuality());
        assertEquals("Test Desired Item", result.getDesiredItem());
        assertEquals(Collections.emptyList(), result.getImageUrls());
        assertEquals(user.getId(), result.getOwnerId());
        assertEquals("Test User", result.getOwnerNickname());
        assertEquals(90, result.getOwnerTrustScore());
        assertTrue(result.getLike());
    }

    @Test
    @DisplayName("교환하기 페이지 조회 실패 - 조회 직전 아이템의 거래 상태가 IN_EXCHANGE로 변경된 경우")
    void testGetExchangeItemFromOwner_Fail_Trading () {
        Long itemId = 1L;
        Long userId = 2L;
        User user = User.builder().id(1L).nickname("Test User").build();
        Category category = new Category("의류");
        ExchangeItem exchangeItem = ExchangeItem.builder()
                .id(itemId)
                .name("Test Item")
                .description("Test Description")
                .category(category)
                .itemQuality(ItemQuality.NEW)
                .user(user)
                .deposit(5000)
                .size("M")
                .brand("Test Brand")
                .desiredItem("Test Desired Item")
                .tradeStatus(TradeStatus.IN_EXCHANGE)
                .isDeleted(false)
                .build();
        try {
            Field createdAtField = BaseEntity.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(exchangeItem, LocalDateTime.of(2023, 12, 1, 12, 0));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Reflection failed", e);
        }

        when(exchangeItemRepository.findById(itemId)).thenReturn(Optional.of(exchangeItem));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            exchangeItemService.getExchangeItemFromOwner(itemId, userId);
        });

        assertEquals(ErrorCode.ITEM_NOT_AVAILABLE, exception.getErrorCode());
        assertEquals("해당 상품이 교환가능 상태가 아닙니다.", exception.getMessage());
    }

    @Test
    @DisplayName("교환할 내 물품 선택 페이지 조회 성공")
    void testGetExchangeItemChoicePage_Success() {
        Long userId = 1L;
        int page = 1;
        int size = 10;

        User user = User.builder().id(userId).build();
        ExchangeItem exchangeItem = mock(ExchangeItem.class);
        Page<ExchangeItem> items = new PageImpl<>(List.of(exchangeItem));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(exchangeItemRepository.findAvailableItemsByUserIdOrderByModifiedAt(userId, PageRequest.of(page - 1, size))).thenReturn(items);
        when(imageService.getFirstImagesByItemIds(any(), any())).thenReturn(Map.of(1L, "http://example.com/image.jpg"));
        when(exchangeItem.getId()).thenReturn(1L);
        when(exchangeItem.getCreatedAt()).thenReturn(LocalDateTime.now());

        GetExchangeItemRespDto result = exchangeItemService.getExchangeItemChoicePage(userId, page, size);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getExchangeItemId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getImageUrl()).isEqualTo("http://example.com/image.jpg");
    }

}