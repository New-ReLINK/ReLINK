package com.my.relink.service;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.trade.dto.request.AddressReqDto;
import com.my.relink.controller.trade.dto.request.TrackingNumberReqDto;
import com.my.relink.controller.trade.dto.response.*;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.ImageRepository;
import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.point.pointHistory.repository.PointHistoryRepository;
import com.my.relink.domain.point.repository.PointRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.trade.repository.TradeRepository;
import com.my.relink.domain.user.Address;
import com.my.relink.domain.user.Role;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.util.DateTimeUtil;
import com.my.relink.util.DummyObject;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Null;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest extends DummyObject {
    @Mock
    private TradeRepository tradeRepository;
    @Mock
    private UserTrustScoreService userTrustScoreService;
    @Mock
    private ImageService imageService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PointHistoryRepository pointHistoryRepository;
    @Mock
    private PointRepository pointRepository;
    @Mock
    private PointTransactionService pointTransactionService;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private DateTimeUtil dateTimeUtil;

    @InjectMocks
    private TradeService tradeService;

    @Nested
    @DisplayName("거래 상세 조회 테스트")
    class RetrieveTradeDetail {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {
            @Test
            @DisplayName("거래 상세 정보를 정상적으로 조회한다")
            void retrieveTradeDetail_success() {
                User user = mockRequesterUser();
                User partner = mockOwnerUser();

                Trade trade = mockTrade(partner, user);
                String imageUrl = "image";
                int trustScore = 80;

                when(tradeRepository.findByIdWithItemsAndUser(trade.getId())).thenReturn(Optional.of(trade));
                when(imageService.getExchangeItemUrl(trade.getRequesterExchangeItem())).thenReturn(imageUrl);
                when(userTrustScoreService.getTrustScore(partner)).thenReturn(trustScore);

                TradeInquiryDetailRespDto result = tradeService.getTradeInquiryDetail(trade.getId(), user.getId());

                assertAll(
                        () -> assertNotNull(result),
                        () -> verify(imageService).getExchangeItemUrl(trade.getRequesterExchangeItem()),
                        () -> verify(userTrustScoreService).getTrustScore(partner),
                        () -> assertEquals(imageUrl, result.getExchangeItemInfoDto().getRequestedItem().getImgUrl()),
                        () -> assertEquals(trustScore, result.getTradePartnerInfoDto().getTrustScore())
                );
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {
            @Test
            @DisplayName("존재하지 않는 거래 id로 조회 시 예외가 발생한다")
            void retrieveTradeDetail_WhenTradeNotFound_ThrowsException() {
                Long tradeId = 1L;
                User user = mockRequesterUser();

                when(tradeRepository.findByIdWithItemsAndUser(tradeId))
                        .thenReturn(Optional.empty());

                BusinessException exception = assertThrows(BusinessException.class,
                        () -> tradeService.getTradeInquiryDetail(tradeId, user.getId()));
                assertEquals(exception.getErrorCode(), ErrorCode.TRADE_NOT_FOUND);
            }

            @Test
            @DisplayName("권한이 없는 사용자가 조회 시 예외가 발생한다")
            void retrieveTradeDetail_WhenAccessDenied_ThrowsException() {
                User user = mockRequesterUser();
                Trade trade = mockTrade(mockOwnerUser(), mockOwnerUser());

                when(tradeRepository.findByIdWithItemsAndUser(trade.getId())).thenReturn(Optional.of(trade));

                BusinessException exception = assertThrows(BusinessException.class,
                        () -> tradeService.getTradeInquiryDetail(trade.getId(), user.getId()));
                assertEquals(exception.getErrorCode(), ErrorCode.TRADE_ACCESS_DENIED);
            }
        }
    }


    @Test
    @DisplayName("채팅방 이전 대화 내역 조회: 정상 케이스")
    void getChatRoomMessage_success() {


    }

    @Test
    @DisplayName("교환 신청 : 성공 케이스")
    void testRequestTrade() {
        // given
        Long tradeId = 1L;
        AuthUser authUser = new AuthUser(12L, "test@email.com", Role.USER);
        User currentUser = mockRequesterUser();
        Trade trade = mockTrade(mockOwnerUser(), currentUser);

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(currentUser));
        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));

        // when
        TradeRequestRespDto result = tradeService.requestTrade(tradeId, authUser);

        // then
        assertNotNull(result);
        verify(pointTransactionService).deductPoints(tradeId, currentUser);
        //verify(tradeRepository).save(trade);
    }

    @Test
    @DisplayName("교환 신청 취소 : 성공 케이스")
    void testCancelTradeRequest() {
        // given
        Long tradeId = 1L;
        AuthUser authUser = new AuthUser(12L, "test@email.com", Role.USER);
        User currentUser = mockRequesterUser();
        Trade trade = mockTrade(mockOwnerUser(), currentUser);

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(currentUser));
        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));

        // when
        tradeService.cancelTradeRequest(tradeId, authUser);

        // then
        verify(pointTransactionService).restorePoints(tradeId, currentUser);
        verify(tradeRepository).save(trade);
    }

    @Test
    @DisplayName("교환 수락 시 요청자 주소 입력 : 성공 케이스")
    void testCreateAddressAsRequester() {
        // given
        Long tradeId = 1L;
        AuthUser authUser = new AuthUser(12L, "requester@email.com", Role.USER);
        AddressReqDto reqDto = mock(AddressReqDto.class);
        User currentUser = mockRequesterUser();
        User ownerUser = mockOwnerUser();
        Trade trade = mockTrade(ownerUser, currentUser, true, true);
        Address address = mock(Address.class);  // 요청자 주소 객체 Mock

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(currentUser));
        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));
        when(reqDto.toRequesterAddressEntity()).thenReturn(address);  // 요청자 주소 생성 반환

        // when
        AddressRespDto result = tradeService.createAddress(tradeId, reqDto, authUser);

        // then
        assertNotNull(result);
        verify(tradeRepository).save(trade);
    }

    @Test
    @DisplayName("교환 수락 시 소유자 주소 입력 : 성공 케이스")
    void testCreateAddressAsOwner() {
        // given
        Long tradeId = 1L;
        AuthUser authUser = new AuthUser(11L, "owner@email.com", Role.USER);  // 소유자 정보
        AddressReqDto reqDto = mock(AddressReqDto.class);
        User currentUser = mockOwnerUser();  // 소유자
        User requesterUser = mockRequesterUser();  // 요청자
        Trade trade = mockTrade(currentUser, requesterUser, true, true);
        Address address = mock(Address.class);  // 소유자 주소 객체 Mock

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(currentUser));
        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));
        when(reqDto.toOwnerAddressEntity()).thenReturn(address);  // 소유자 주소 생성 반환

        // when
        AddressRespDto result = tradeService.createAddress(tradeId, reqDto, authUser);

        // then
        assertNotNull(result);  // 결과가 null이 아님을 확인
        verify(tradeRepository).save(trade);  // Trade 객체가 저장되는지 확인
    }

    @Test
    @DisplayName("교환 수령 완료 : 성공 케이스")
    void testCompleteTrade() {
        // given
        Long tradeId = 1L;
        User owner = mockOwnerUser();
        User requester = mockRequesterUser();
        Trade trade = mockTrade(owner, requester, true, true); // 요청자가 요청 완료된 상태

        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));

        // Mock PointTransactionService
        doNothing().when(pointTransactionService).restorePointsForAllTraders(trade, 10000);

        // When
        TradeCompleteRespDto response = tradeService.completeTrade(tradeId, new AuthUser(requester.getId(), "test@email.com", Role.USER));

        // Then
        assertEquals(tradeId, response.getTradeId());

        verify(tradeRepository, times(1)).findById(tradeId);
        verify(pointTransactionService, times(1)).restorePointsForAllTraders(trade, 10000);
    }

    @Test
    @DisplayName("교환 진행 페이지 : 운송장 입력받기 성공케이스")
    void testGetTrackingNumber_BothTrackingNumbersSet_TradeStatusUpdated() {
        // Given
        Long tradeId = 1L;
        User requester = mockRequesterUser();
        User owner = mockOwnerUser();
        Trade trade = mockTrade(owner, requester);
        trade.updateOwnerTrackingNumber("OWN123456"); // 소유자의 운송장 번호 설정
        TrackingNumberReqDto reqDto = new TrackingNumberReqDto("REQ123456");

        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));

        // When
        tradeService.getExchangeItemTrackingNumber(tradeId, reqDto, new AuthUser(requester.getId(), "test@email.com", Role.USER));

        // Then
        assertEquals("REQ123456", trade.getRequesterTrackingNumber());
        assertEquals("OWN123456", trade.getOwnerTrackingNumber());
        assertEquals(TradeStatus.IN_DELIVERY, trade.getTradeStatus());
        verify(tradeRepository, times(1)).save(trade);
    }

    @Test
    @DisplayName("교환 진행 페이지 : 요청자만 운송장 입력받기 성공케이스")
    void testGetTrackingNumber_RequesterTrackingNumber_TradeStatusUpdated() {
        // Given
        Long tradeId = 1L;
        User requester = mockRequesterUser();
        User owner = mockOwnerUser();
        Trade trade = mockTrade(owner, requester);
        trade.updateOwnerTrackingNumber("");
        trade.updateRequesterTrackingNumber("");
        TrackingNumberReqDto reqDto = new TrackingNumberReqDto("REQ123456");

        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));

        // When
        tradeService.getExchangeItemTrackingNumber(tradeId, reqDto, new AuthUser(requester.getId(), "test@email.com", Role.USER));

        // Then
        assertEquals("REQ123456", trade.getRequesterTrackingNumber());
        assertEquals("", trade.getOwnerTrackingNumber());
        assertNotEquals(TradeStatus.IN_DELIVERY, trade.getTradeStatus());
        verify(tradeRepository, times(1)).save(trade);
    }

    @Test
    @DisplayName("교환 진행 페이지 : 소유자만 운송장 입력받기 성공케이스")
    void testGetTrackingNumber_OwnerTrackingNumber_TradeStatusUpdated() {
        // Given
        Long tradeId = 1L;
        User requester = mockRequesterUser();
        User owner = mockOwnerUser();
        Trade trade = mockTrade(owner, requester);
        trade.updateOwnerTrackingNumber("");
        trade.updateRequesterTrackingNumber("");
        TrackingNumberReqDto reqDto = new TrackingNumberReqDto("OWN123456");

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));

        // When
        tradeService.getExchangeItemTrackingNumber(tradeId, reqDto, new AuthUser(owner.getId(), "test@email.com", Role.USER));

        // Then
        assertEquals("OWN123456", trade.getOwnerTrackingNumber());
        assertEquals("", trade.getRequesterTrackingNumber());
        assertNotEquals(TradeStatus.IN_DELIVERY, trade.getTradeStatus());
        verify(tradeRepository, times(1)).save(trade);
    }

    @Test
    @DisplayName("교환 진행 페이지 : 운송장 입력받기 실패 케이스")
    void testGetTrackingNumber_InvalidTrackingNumber() {
        // Given
        Long tradeId = 1L;
        TrackingNumberReqDto reqDto = new TrackingNumberReqDto(""); // 빈 운송장 번호

        // When & Then
        assertThrows(BusinessException.class, () ->
                tradeService.getExchangeItemTrackingNumber(tradeId, reqDto, new AuthUser(12L, "test@email.com", Role.USER)));
    }

    @Test
    @DisplayName("교환 진행 페이지 : 조회 성공 케이스")
    void testFindTradeCompletionInfo_success(){
        Long tradeId = 1L;
        User requester = mockRequesterUser();
        User owner = mockOwnerUser();
        Trade trade = mockTrade(owner, requester, true, true, true, true);

        ExchangeItem myExchangeItem;
        ExchangeItem partnerExchangeItem;

        if (trade.isRequester(requester.getId())) {
            myExchangeItem = trade.getRequesterExchangeItem();
            partnerExchangeItem = trade.getOwnerExchangeItem();
        } else {
            myExchangeItem = trade.getOwnerExchangeItem();
            partnerExchangeItem = trade.getRequesterExchangeItem();
        }

        String myImageUrl = "http://example.com/my-image.jpg";
        String partnerImageUrl = "http://example.com/partner-image.jpg";

        User partnerUser = trade.getPartner(requester.getId());  // 거래 상대방 (소유자)

        Mockito.when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        Mockito.when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Mockito.when(tradeRepository.findTradeWithDetails(tradeId)).thenReturn(Optional.of(trade));
        Mockito.when(imageService.getExchangeItemUrl(myExchangeItem)).thenReturn(myImageUrl);
        Mockito.when(imageService.getExchangeItemUrl(partnerExchangeItem)).thenReturn(partnerImageUrl);
        Mockito.when(userRepository.findById(trade.getPartner(requester.getId()).getId())).thenReturn(Optional.of(partnerUser));
        Mockito.when(dateTimeUtil.getTradeStatusFormattedTime(trade.getModifiedAt()))
                .thenReturn("2024년 12월 12일 14:30");

        TradeCompletionRespDto result = tradeService.findCompleteTradeInfo(tradeId, new AuthUser(requester.getId(), "test@email.com", Role.USER));

        assertNotNull(result);

        assertEquals("requester item", result.getMyItem().getItemName());
        assertEquals(ItemQuality.NEW, result.getMyItem().getItemQuality());
        assertEquals(13L, result.getMyItem().getItemId());
        assertEquals("http://example.com/my-image.jpg", result.getMyItem().getItemImageUrl());

        assertEquals("owner item", result.getPartnerItem().getItemName());
        assertEquals(ItemQuality.NEW, result.getPartnerItem().getItemQuality());
        assertEquals(10L, result.getPartnerItem().getItemId());
        assertEquals("http://example.com/partner-image.jpg", result.getPartnerItem().getItemImageUrl());

        assertNotNull(result.getTradeStatusInfo().getCompletedAt());
        assertEquals(trade.getTradeStatus(), result.getTradeStatusInfo().getTradeStatus());
    }

    @Test
    @DisplayName("교환 진행 페이지 : 사용자 조회 실패 케이스")
    void testFindTradeCompletionInfo_userNotFound() {
        Long tradeId = 1L;
        User requester = mockRequesterUser();

        Mockito.when(userRepository.findById(requester.getId())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tradeService.findCompleteTradeInfo(tradeId, new AuthUser(requester.getId(), "test@email.com", Role.USER)),
                "사용자가 존재하지 않는 경우 예외가 발생해야 한다."
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("교환 진행 페이지 : 거래 조회 실패 케이스")
    void testFindTradeCompletionInfo_tradeNotFound() {
        Long tradeId = 1L;
        User requester = mockRequesterUser();


        Mockito.when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        Mockito.when(tradeRepository.findById(tradeId)).thenReturn(Optional.empty());  // 거래가 없음

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tradeService.findCompleteTradeInfo(tradeId, new AuthUser(requester.getId(), "test@email.com", Role.USER)),
                "거래가 존재하지 않는 경우 예외가 발생해야 한다."
        );

        assertEquals(ErrorCode.TRADE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("교환 취소 페이지 : 교환 취소 성공 케이스")
    void testCancelTrade_success() {
        Long tradeId = 1L;
        User requester = mockRequesterUser();
        User owner = mockOwnerUser();

        // Trade를 Mock 객체로 생성
        Trade trade = mockTrade(owner, requester, true, true, true, true);
        ExchangeItem partnerExchangeItem;

        if (trade.isRequester(requester.getId())) {
            partnerExchangeItem = trade.getOwnerExchangeItem();
        } else {
            partnerExchangeItem = trade.getRequesterExchangeItem();
        }
        String partnerImage = "http://example.com/partner-image.jpg";

        Mockito.when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        Mockito.when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));
        Mockito.when(imageService.getExchangeItemUrl(partnerExchangeItem)).thenReturn("http://example.com/partner-image.jpg");

        // 서비스 호출
        TradeCancelRespDto result = tradeService.cancelTrade(tradeId, new AuthUser(requester.getId(), "test@email.com", Role.USER));

        // 검증
        assertNotNull(result);
        assertEquals(partnerExchangeItem.getName(), result.getPartnerExchangeItemName());
        assertEquals(owner.getNickname(), result.getPartnerNickname());
        assertEquals(partnerImage, result.getPartnerExchangeItemImageUrl());
    }

    @Test
    @DisplayName("교환 취소 페이지 : 거래가 존재하지 않는 경우 실패 케이스")
    void testCancelTrade_tradeNotFound() {
        Long tradeId = 1L;
        User requester = mockRequesterUser();

        // 거래가 없으므로 tradeRepository는 빈 Optional 반환
        Mockito.when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        Mockito.when(tradeRepository.findById(tradeId)).thenReturn(Optional.empty());  // 거래 없음

        // 예외 발생을 검증
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tradeService.cancelTrade(tradeId, new AuthUser(requester.getId(), "test@email.com", Role.USER)),
                "거래가 존재하지 않는 경우 예외가 발생해야 한다."
        );

        // 예외 메시지 확인
        assertEquals(ErrorCode.TRADE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("교환 취소 페이지 : 사용자 정보가 존재하지 않는 경우 실패 케이스")
    void testCancelTrade_userNotFound() {
        Long tradeId = 1L;

        // 사용자 정보가 없으므로 userRepository는 빈 Optional 반환
        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.empty());  // 사용자 없음

        // 예외 발생을 검증
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tradeService.cancelTrade(tradeId, new AuthUser(1L, "test@email.com", Role.USER)),
                "사용자 정보가 존재하지 않는 경우 예외가 발생해야 한다."
        );

        // 예외 메시지 확인
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }
}