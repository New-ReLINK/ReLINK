package com.my.relink.service;

import com.my.relink.controller.report.dto.request.TradeReportCreateReqDto;
import com.my.relink.domain.report.Report;
import com.my.relink.domain.report.ReportReason;
import com.my.relink.domain.report.ReportType;
import com.my.relink.domain.report.repository.ReportRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private TradeService tradeService;

    @Mock
    private ReportRepository reportRepository;

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