package com.my.relink.service;

import com.my.relink.controller.report.dto.request.ExchangeItemReportCreateReqDto;
import com.my.relink.controller.report.dto.request.TradeReportCreateReqDto;
import com.my.relink.controller.report.dto.response.TradeInfoRespDto;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.report.Report;
import com.my.relink.domain.report.ReportReason;
import com.my.relink.domain.report.ReportType;
import com.my.relink.domain.report.repository.ReportRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.util.DateTimeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private TradeService tradeService;

    @Mock
    private ReportRepository reportRepository;
    @Mock
    private ExchangeItemService exchangeItemService;

    @Mock
    private ImageService imageService;

    @Mock
    private DateTimeUtil dateTimeUtil;


    @Nested
    @DisplayName("신고 전 교환 상품 조회 테스트")
    class GetExchangeItemInfoForReport{


        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase{

        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCase{

        }
    }


    @Nested
    @DisplayName("신고 전 거래 정보 조회 테스트")
    class GetTradeInfoForReport{

        private Long tradeId = 1L;
        private Long ownerId = 10L;
        private Long requesterId = 20L;
        private Trade trade;
        private User owner;
        private User requester;
        private ExchangeItem ownerItem;
        private ExchangeItem requesterItem;
        private LocalDateTime now = LocalDateTime.now();

        private String exchangedStartDate = "2024-12-13";

        @BeforeEach
        void setUp() {
            owner = User.builder()
                    .id(ownerId)
                    .nickname("owner")
                    .build();

            requester = User.builder()
                    .id(requesterId)
                    .nickname("requester")
                    .build();

            ownerItem = ExchangeItem.builder()
                    .id(1L)
                    .name("owner 상품")
                    .user(owner)
                    .build();

            requesterItem = ExchangeItem.builder()
                    .id(2L)
                    .name("requester 상품")
                    .user(requester)
                    .build();

            trade = Trade.builder()
                    .id(tradeId)
                    .ownerExchangeItem(ownerItem)
                    .requester(requester)
                    .requesterExchangeItem(requesterItem)
                    .build();

            ReflectionTestUtils.setField(trade, "createdAt", now);
        }

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            private String imageUrl = "imageUrl";

            @BeforeEach
            void setUp(){
                when(tradeService.findByIdWithItemsAndUsersOrFail(tradeId)).thenReturn(trade);
            }

            @Test
            @DisplayName("owner가 requester의 정보를 조회할 수 있다")
            void owner_can_get_requester_info() {
                when(tradeService.getTradePartnerIncludeWithdrawn(ownerId, tradeId)).thenReturn(requester);
                when(exchangeItemService.findByUserIdIncludeWithdrawn(requesterId)).thenReturn(requesterItem);
                when(imageService.getExchangeItemThumbnailUrl(requesterItem)).thenReturn(imageUrl);
                when(dateTimeUtil.getExchangeStartFormattedTime(now)).thenReturn(exchangedStartDate);

                TradeInfoRespDto result = reportService.getTradeInfoForReport(tradeId, ownerId);

                assertAll(() -> {
                    assertEquals(result.getPartnerExchangeItemId(), requesterItem.getId());
                    assertEquals(result.getPartnerNickname(), requester.getNickname());
                    assertEquals(result.getPartnerExchangeItemImageUrl(), imageUrl);
                    assertEquals(result.getExchangeStartDate(), exchangedStartDate);
                });
            }

            @Test
            @DisplayName("requester가 owner의 정보를 조회할 수 있다")
            void requester_can_get_owner_info() {
                when(tradeService.getTradePartnerIncludeWithdrawn(requesterId, tradeId)).thenReturn(owner);
                when(exchangeItemService.findByUserIdIncludeWithdrawn(ownerId)).thenReturn(ownerItem);
                when(imageService.getExchangeItemThumbnailUrl(ownerItem)).thenReturn(imageUrl);
                when(dateTimeUtil.getExchangeStartFormattedTime(now)).thenReturn(exchangedStartDate);

                TradeInfoRespDto result = reportService.getTradeInfoForReport(tradeId, requesterId);

                assertAll(() -> {
                    assertEquals(result.getPartnerExchangeItemId(), ownerItem.getId());
                    assertEquals(result.getPartnerNickname(), owner.getNickname());
                    assertEquals(result.getPartnerExchangeItemImageUrl(), imageUrl);
                    assertEquals(result.getExchangeStartDate(), exchangedStartDate);
                });
            }


            @Test
            @DisplayName("상대방이 탈퇴한 경우에도 조회할 수 있다")
            void can_get_partner_info_when_partner_withdrawn(){
                owner.changeIsDeleted();

                when(tradeService.getTradePartnerIncludeWithdrawn(requesterId, tradeId)).thenReturn(owner);
                when(exchangeItemService.findByUserIdIncludeWithdrawn(ownerId)).thenReturn(ownerItem);
                when(imageService.getExchangeItemThumbnailUrl(ownerItem)).thenReturn(imageUrl);
                when(dateTimeUtil.getExchangeStartFormattedTime(now)).thenReturn(exchangedStartDate);

                TradeInfoRespDto result = reportService.getTradeInfoForReport(tradeId, requesterId);

                assertAll(() -> {
                    assertEquals(result.getPartnerExchangeItemId(), ownerItem.getId());
                    assertEquals(result.getPartnerNickname(), "탈퇴한 사용자");
                    assertEquals(result.getPartnerExchangeItemImageUrl(), imageUrl);
                    assertEquals(result.getExchangeStartDate(), exchangedStartDate);
                });

            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {

            @Test
            @DisplayName("존재하지 않는 거래면 예외가 발생한다")
            void fail_if_trade_not_found() {
                given(tradeService.findByIdWithItemsAndUsersOrFail(tradeId))
                        .willThrow(new BusinessException(ErrorCode.TRADE_NOT_FOUND));

                assertThatThrownBy(() -> reportService.getTradeInfoForReport(tradeId, ownerId))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TRADE_NOT_FOUND);
            }

            @Test
            @DisplayName("거래 당사자가 아니라면 예외가 발생한다")
            void fail_if_not_trade_participant(){
                Long invalidUserId = 11L;
                when(tradeService.findByIdWithItemsAndUsersOrFail(tradeId)).thenReturn(trade);

                assertThatThrownBy(() -> reportService.getTradeInfoForReport(tradeId, invalidUserId))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TRADE_ACCESS_DENIED);
            }

            @Test
            @DisplayName("거래 상대방을 찾을 수 없으면 예외가 발생한다")
            void fail_if_partner_not_found() {
                trade = Trade.builder()
                        .id(tradeId)
                        .ownerExchangeItem(ownerItem)
                        .requester(null) //탈퇴 회원 보관 기간을 지나 아예 삭제된 경우라 가정
                        .requesterExchangeItem(null)
                        .build();

                when(tradeService.findByIdWithItemsAndUsersOrFail(tradeId)).thenReturn(trade);
                given(tradeService.getTradePartnerIncludeWithdrawn(ownerId, tradeId))
                        .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

                assertThatThrownBy(() -> reportService.getTradeInfoForReport(tradeId, ownerId))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
            }

        }
    }



    @Nested
    @DisplayName("교환 상품 신고 생성 테스트")
    class CreateExchangeItemReport{

        private ExchangeItemReportCreateReqDto reportDto;

        private ExchangeItem exchangeItem;

        private Long exchangeItemId = 1L;
        private Long ownerId = 1L;
        private User owner;

        @BeforeEach
        void setUp(){
            exchangeItem = mock(ExchangeItem.class);
            reportDto = new ExchangeItemReportCreateReqDto(
                    ReportReason.ILLEGAL_ITEM.toString(),
                    "신고합니다"
            );
        }

        @Test
        @DisplayName("교환 상품 신고에 성공한다")
        void success(){
            ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);

            when(exchangeItem.getId()).thenReturn(exchangeItemId);
            owner = mock(User.class);
            when(exchangeItem.getUser()).thenReturn(owner);
            when(exchangeItem.getUser().getId()).thenReturn(ownerId);
            when(exchangeItemService.findByIdOrFail(exchangeItemId)).thenReturn(exchangeItem);

            reportService.createExchangeItemReport(exchangeItemId, reportDto);

            verify(reportRepository).save(reportCaptor.capture());
            Report savedReport = reportCaptor.getValue();
            assertAll(() -> {
                assertEquals(savedReport.getReportType(), ReportType.ITEM);
                assertEquals(savedReport.getReportReason(), ReportReason.ILLEGAL_ITEM);
                assertEquals(savedReport.getTargetUserId(), ownerId);
                assertEquals(savedReport.getEntityId(), exchangeItemId);
                assertEquals(savedReport.getDescription(), "신고합니다");
            });

        }

        @Test
        @DisplayName("존재하지 않는 교환 상품 id를 신고 시 예외가 발생한다")
        void throwsException_whenExchangeItemNotFound(){
            when(exchangeItemService.findByIdOrFail(exchangeItemId))
                    .thenThrow(new BusinessException(ErrorCode.EXCHANGE_ITEM_NOT_FOUND));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    reportService.createExchangeItemReport(exchangeItemId, reportDto));

            verify(reportRepository, never()).save(any());
            assertEquals(exception.getErrorCode(), ErrorCode.EXCHANGE_ITEM_NOT_FOUND);
        }



    }



    @Nested
    @DisplayName("거래 신고 생성 테스트")
    class CreateTradeReport{

        private User reporter;
        private User partner;
        private Trade trade;
        private TradeReportCreateReqDto reportDto;
        private static final Long tradeId = 1L;
        private static final Long reporterId = 10L;
        private static final Long partnerId = 20L;
        @BeforeEach
        void setUp() {
            reporter = mock(User.class);
            partner = mock(User.class);
            trade = mock(Trade.class);
            reportDto = new TradeReportCreateReqDto(
                    ReportReason.FALSE_INFORMATION.toString(),
                    "신고 내용입니다"
            );
        }

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @BeforeEach
            void setUp(){
                when(partner.getId()).thenReturn(partnerId);
                when(trade.getId()).thenReturn(tradeId);
                when(trade.getPartner(reporterId)).thenReturn(partner);
            }

            @Test
            @DisplayName("거래 신고에 성공한다")
            void success() {
                ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
                when(tradeService.findByIdWithUsersOrFail(tradeId)).thenReturn(trade);
                when(reportRepository.findByEntityIdAndReportTypeAndTargetUserId(
                        tradeId,
                        ReportType.TRADE,
                        partnerId
                )).thenReturn(Optional.empty());
                doNothing().when(trade).validateAccess(reporterId);

                reportService.createTradeReport(tradeId, reporterId, reportDto);

                verify(tradeService).findByIdWithUsersOrFail(tradeId);
                verify(trade).validateAccess(reporterId);
                verify(trade).getPartner(reporterId);
                verify(reportRepository).save(reportCaptor.capture());

                Report savedReport = reportCaptor.getValue();

                assertAll(
                        () -> assertEquals(ReportType.TRADE, savedReport.getReportType()),
                        () -> assertEquals(tradeId, savedReport.getEntityId()),
                        () -> assertEquals(partnerId, savedReport.getTargetUserId()),
                        () -> assertEquals(ReportReason.FALSE_INFORMATION, savedReport.getReportReason()),
                        () -> assertEquals("신고 내용입니다", savedReport.getDescription())
                );
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase {
            @Test
            @DisplayName("존재하지 않는 거래 id를 신고 시 예외가 발생한다")
            void throwsException_whenTradeNotFound() {
                when(tradeService.findByIdWithUsersOrFail(tradeId))
                        .thenThrow(new BusinessException(ErrorCode.TRADE_NOT_FOUND));

                assertThrows(BusinessException.class, () ->
                        reportService.createTradeReport(tradeId, reporterId, reportDto));

                verify(reportRepository, never()).save(any());
            }

            @Test
            @DisplayName("신고자가 거래 참여자가 아닐 시 예외가 발생한다")
            void throwsException_whenInvalidAccess(){
                when(tradeService.findByIdWithUsersOrFail(tradeId)).thenReturn(trade);
                doThrow(new BusinessException(ErrorCode.TRADE_ACCESS_DENIED))
                        .when(trade).validateAccess(reporterId);

                assertThrows(BusinessException.class, () ->
                        reportService.createTradeReport(tradeId, reporterId, reportDto));

                verify(reportRepository, never()).save(any());
            }

            @Test
            @DisplayName("동일 신고 대상자에 대해 중복 신고를 요청할 시 예외가 발생한다")
            void throwsException_whenAlreadyReportedReport(){
                Report report = Report.builder()
                        .entityId(tradeId)
                        .targetUserId(partnerId)
                        .id(1L)
                        .build();
                when(tradeService.findByIdWithUsersOrFail(tradeId)).thenReturn(trade);
                when(reportRepository.findByEntityIdAndReportTypeAndTargetUserId(
                        tradeId,
                        ReportType.TRADE,
                        partnerId
                )).thenReturn(Optional.of(report));
                when(partner.getId()).thenReturn(partnerId);
                when(trade.getPartner(reporterId)).thenReturn(partner);

                BusinessException exception = assertThrows(BusinessException.class, () ->
                        reportService.createTradeReport(tradeId, reporterId, reportDto));

                verify(reportRepository, never()).save(any());
                assertEquals(exception.getErrorCode(), ErrorCode.ALREADY_REPORTED_TRADE);
            }

        }
    }


}