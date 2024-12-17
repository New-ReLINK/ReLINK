package com.my.relink.service.payment;

import com.my.relink.client.tosspayments.dto.response.TossPaymentRespDto;
import com.my.relink.client.tosspayments.ex.TossPaymentErrorCode;
import com.my.relink.client.tosspayments.ex.TossPaymentException;
import com.my.relink.client.tosspayments.ex.badRequest.TossPaymentBadRequestException;
import com.my.relink.controller.payment.dto.request.PaymentReqDto;
import com.my.relink.controller.payment.dto.response.PaymentRespDto;
import com.my.relink.domain.payment.Payment;
import com.my.relink.domain.payment.PaymentCancelReason;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.pointHistory.PointHistory;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.my.relink.client.tosspayments.dto.response.TossPaymentRespDto.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentProcessServiceTest {

    @InjectMocks
    private PaymentProcessService paymentProcessService;

    @Mock
    private PaymentService paymentService;

    private PaymentReqDto paymentReqDto;
    private User user;
    private TossPaymentRespDto tossPaymentRespDto;
    private Payment payment;
    private PointHistory pointHistory;

    private String paymentKey = "paymentKey";
    private String orderId = "orderId";
    private Integer amount = 10000;

    private Point point;

    @BeforeEach
    void setUp() {
        paymentReqDto = PaymentReqDto.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(amount)
                .build();

        user = User.builder()
                .id(1L)
                .email("riku1234@naver.com")
                .build();

        tossPaymentRespDto = builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .totalAmount(amount)
                .status("DONE")
                .build();

        payment = Payment.builder()
                .id(1L)
                .user(user)
                .amount(amount)
                .status("DONE")
                .build();

        point = Point.builder().amount(amount).user(user).build();

        pointHistory = PointHistory.createChargeHistory(point);
    }

    @Test
    @DisplayName("결제 프로세스 정상 케이스")
    void processPayment_Success() {
        when(paymentService.validateUserAndPayment(paymentReqDto)).thenReturn(user);
        when(paymentService.confirmPayment(paymentReqDto)).thenReturn(tossPaymentRespDto);
        when(paymentService.savePaymentInfo(paymentReqDto, tossPaymentRespDto, user)).thenReturn(payment);
        when(paymentService.chargePointWithHistory(user, payment, paymentReqDto)).thenReturn(pointHistory);

        PaymentRespDto result = paymentProcessService.processPayment(paymentReqDto);

        assertAll(() -> {
            assertThat(result).isNotNull();
            verify(paymentService).validateUserAndPayment(paymentReqDto);
            verify(paymentService).confirmPayment(paymentReqDto);
            verify(paymentService).savePaymentInfo(paymentReqDto, tossPaymentRespDto, user);
            verify(paymentService).chargePointWithHistory(user, payment, paymentReqDto);
        });
    }

    @Test
    @DisplayName("유저 검증 실패 시 예외가 발생한다")
    void processPayment_fail_when_userNotFound(){
        when(paymentService.validateUserAndPayment(paymentReqDto))
                .thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> paymentProcessService.processPayment(paymentReqDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        assertAll(() -> {
            verify(paymentService).validateUserAndPayment(paymentReqDto);
            verify(paymentService, never()).confirmPayment(any());
            verify(paymentService, never()).savePaymentInfo(any(), any(), any());
            verify(paymentService, never()).chargePointWithHistory(any(), any(), any());
        });
    }

    @Test
    @DisplayName("결제 정보 검증 실패 시 예외가 발생한다")
    void processPayment_fail_when_paymentInfo_invalid(){
        when(paymentService.validateUserAndPayment(paymentReqDto))
                .thenThrow(new BusinessException(ErrorCode.PAYMENT_INFO_MISMATCH));

        assertThatThrownBy(() -> paymentProcessService.processPayment(paymentReqDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_INFO_MISMATCH);

        assertAll(() -> {
            verify(paymentService).validateUserAndPayment(paymentReqDto);
            verify(paymentService, never()).confirmPayment(any());
            verify(paymentService, never()).savePaymentInfo(any(), any(), any());
            verify(paymentService, never()).chargePointWithHistory(any(), any(), any());
        });
    }

    @Test
    @DisplayName("토스 결제 승인 실패: TossPaymentException 이 발생한 경우")
    void processPayment_fail_when_tossPaymentConfirm_throws_exception() {
        when(paymentService.validateUserAndPayment(paymentReqDto)).thenReturn(user);
        when(paymentService.confirmPayment(paymentReqDto))
                .thenThrow(new TossPaymentBadRequestException(TossPaymentErrorCode.ALREADY_PROCESSED_PAYMENT, paymentKey));

        assertThatThrownBy(() -> paymentProcessService.processPayment(paymentReqDto))
                .isInstanceOf(TossPaymentException.class)
                .hasFieldOrPropertyWithValue("errorCode", TossPaymentErrorCode.ALREADY_PROCESSED_PAYMENT);

        assertAll(() -> {
            verify(paymentService).validateUserAndPayment(paymentReqDto);
            verify(paymentService).confirmPayment(paymentReqDto);
            verify(paymentService, never()).savePaymentInfo(any(), any(), any());
            verify(paymentService, never()).chargePointWithHistory(any(), any(), any());
        });
    }

    @Test
    @DisplayName("토스 결제 승인 실패: 예기치 못한 예외가 발생한 경우")
    void processPayment_fail_when_unExpected_exception_occurs(){
        when(paymentService.validateUserAndPayment(paymentReqDto)).thenReturn(user);
        when(paymentService.confirmPayment(paymentReqDto))
                .thenThrow(new RuntimeException("모종의 에러 발생"));

        assertThatThrownBy(() -> paymentProcessService.processPayment(paymentReqDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CRITICAL_PAYMENT_PROCESS_ERROR);

        assertAll(() -> {
            verify(paymentService).validateUserAndPayment(paymentReqDto);
            verify(paymentService).confirmPayment(paymentReqDto);
            verify(paymentService, never()).savePaymentInfo(any(), any(), any());
            verify(paymentService, never()).chargePointWithHistory(any(), any(), any());
        });
    }

    @Test
    @DisplayName("결제 정보 저장 실패: 결제 취소 성공")
    void processPayment_savePaymentInfoFail_but_successfully_cancel_payment(){
        when(paymentService.validateUserAndPayment(paymentReqDto)).thenReturn(user);
        when(paymentService.confirmPayment(paymentReqDto)).thenReturn(tossPaymentRespDto);
        when(paymentService.savePaymentInfo(paymentReqDto, tossPaymentRespDto, user))
                .thenThrow(new BusinessException(ErrorCode.FAIL_TO_SAVE_PAYMENT));

        assertThatThrownBy(() -> paymentProcessService.processPayment(paymentReqDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAIL_TO_SAVE_PAYMENT);

        assertAll(() -> {
            verify(paymentService).validateUserAndPayment(paymentReqDto);
            verify(paymentService).confirmPayment(paymentReqDto);
            verify(paymentService).savePaymentInfo(paymentReqDto, tossPaymentRespDto, user);
            verify(paymentService, never()).chargePointWithHistory(any(), any(), any());
        });
    }

    @Test
    @DisplayName("결제 정보 저장 실패: 결제 취소 실패 - 결제 취소 검증 실패")
    void processPayment_savePaymentInfoFail_and_processCancelPayment_fail(){
        when(paymentService.validateUserAndPayment(paymentReqDto)).thenReturn(user);
        when(paymentService.confirmPayment(paymentReqDto)).thenReturn(tossPaymentRespDto);
        when(paymentService.savePaymentInfo(paymentReqDto, tossPaymentRespDto, user))
                .thenThrow(new BusinessException(ErrorCode.PAYMENT_CANCEL_INCOMPLETE));

        assertThatThrownBy(() -> paymentProcessService.processPayment(paymentReqDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_CANCEL_INCOMPLETE);

        assertAll(() -> {
            verify(paymentService).validateUserAndPayment(paymentReqDto);
            verify(paymentService).confirmPayment(paymentReqDto);
            verify(paymentService).savePaymentInfo(paymentReqDto, tossPaymentRespDto, user);
            verify(paymentService, never()).chargePointWithHistory(any(), any(), any());
        });
    }

    @Test
    @DisplayName("결제 정보 저장 실패: 결제 취소 실패 - 예상치 못한 예외 발생")
    void processPayment_savePaymentInfoFail_and_processCancelPayment_fail_with_unExpected_exception(){
        when(paymentService.validateUserAndPayment(paymentReqDto)).thenReturn(user);
        when(paymentService.confirmPayment(paymentReqDto)).thenReturn(tossPaymentRespDto);
        when(paymentService.savePaymentInfo(paymentReqDto, tossPaymentRespDto, user))
                .thenThrow(new BusinessException(ErrorCode.CRITICAL_PAYMENT_SAVE_ERROR));

        assertThatThrownBy(() -> paymentProcessService.processPayment(paymentReqDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CRITICAL_PAYMENT_SAVE_ERROR);

        assertAll(() -> {
            verify(paymentService).validateUserAndPayment(paymentReqDto);
            verify(paymentService).confirmPayment(paymentReqDto);
            verify(paymentService).savePaymentInfo(paymentReqDto, tossPaymentRespDto, user);
            verify(paymentService, never()).chargePointWithHistory(any(), any(), any());
        });
    }

    @Test
    @DisplayName("포인트 충전 실패 - 결제 취소 성공")
    void processPayment_ChargePointFail_CancelSuccess() {
        when(paymentService.validateUserAndPayment(paymentReqDto)).thenReturn(user);
        when(paymentService.confirmPayment(paymentReqDto)).thenReturn(tossPaymentRespDto);
        when(paymentService.savePaymentInfo(paymentReqDto, tossPaymentRespDto, user)).thenReturn(payment);


        when(paymentService.chargePointWithHistory(user, payment, paymentReqDto))
                .thenThrow(new BusinessException(ErrorCode.FAIL_TO_POINT_CHARGE));

        assertThatThrownBy(() -> paymentProcessService.processPayment(paymentReqDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAIL_TO_POINT_CHARGE);
    }

    @Test
    @DisplayName("포인트 충전 실패 - 결제 취소 실패")
    void processPayment_chargePointFail_CancelFail(){
        when(paymentService.validateUserAndPayment(paymentReqDto)).thenReturn(user);
        when(paymentService.confirmPayment(paymentReqDto)).thenReturn(tossPaymentRespDto);
        when(paymentService.savePaymentInfo(paymentReqDto, tossPaymentRespDto, user)).thenReturn(payment);
        when(paymentService.chargePointWithHistory(user, payment, paymentReqDto))
                .thenThrow(new BusinessException(ErrorCode.CRITICAL_POINT_CHARGE_ERROR));

        assertThatThrownBy(() -> paymentProcessService.processPayment(paymentReqDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CRITICAL_POINT_CHARGE_ERROR);
    }






}