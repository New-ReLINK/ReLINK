package com.my.relink.service;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.trade.dto.request.AddressReqDto;
import com.my.relink.controller.trade.dto.response.AddressRespDto;
import com.my.relink.controller.trade.dto.response.TradeCompleteRespDto;
import com.my.relink.controller.trade.dto.response.TradeInquiryDetailRespDto;
import com.my.relink.controller.trade.dto.response.TradeRequestRespDto;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.pointHistory.PointHistory;
import com.my.relink.domain.point.pointHistory.repository.PointHistoryRepository;
import com.my.relink.domain.point.repository.PointRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.repository.TradeRepository;
import com.my.relink.domain.user.Address;
import com.my.relink.domain.user.Role;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.util.DummyObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private PointTransactionService pointTransactionService;
    @Mock
    private PointHistoryRepository pointHistoryRepository;
    @Mock
    private PointRepository pointRepository;


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
    void getChatRoomMessage_success(){


    }

    @Test
    @DisplayName("교환 신청 : 성공 케이스")
    void testRequestTrade(){
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
        AuthUser authUser = new AuthUser(12L, "test@email.com", Role.USER);
        User currentUser = mockRequesterUser();
        User ownerUser = mockOwnerUser();
        Trade trade = mockTrade(ownerUser, currentUser);
        PointHistory pointHistory = mock(PointHistory.class);
        Point point = mock(Point.class);

        when(userRepository.findById(authUser.getId())).thenReturn(Optional.of(currentUser));
        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));
        when(pointHistoryRepository.findFirstByTradeIdOrderByCreatedAtDesc(tradeId)).thenReturn(Optional.of(pointHistory));
        when(pointRepository.findByUserId(anyLong())).thenReturn(Optional.of(point));

        // when
        TradeCompleteRespDto result = tradeService.completeTrade(tradeId, authUser);

        // then
        assertNotNull(result);
        assertEquals(tradeId, result.getTradeId());

        verify(pointRepository, times(2)).save(point);
        verify(pointHistoryRepository, times(2)).save(any(PointHistory.class));
    }
}