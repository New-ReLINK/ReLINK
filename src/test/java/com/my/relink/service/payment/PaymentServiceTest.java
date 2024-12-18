package com.my.relink.service.payment;

import com.my.relink.client.tosspayments.TossPaymentClient;
import com.my.relink.client.tosspayments.dto.response.TossPaymentRespDto;
import com.my.relink.client.tosspayments.feature.PaymentStatus;
import com.my.relink.controller.payment.dto.request.PaymentReqDto;
import com.my.relink.domain.payment.Payment;
import com.my.relink.domain.payment.repository.PaymentRepository;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.pointHistory.PointHistory;
import com.my.relink.domain.point.pointHistory.repository.PointHistoryRepository;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.service.PointService;
import com.my.relink.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.my.relink.client.tosspayments.dto.response.TossPaymentRespDto.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private TossPaymentClient tossPaymentClient;

    @Mock
    private UserService userService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Mock
    private PointService pointService;

    @Nested
    @DisplayName("포인트 충전 테스트")
    class chargePointWithHistory{

        private User user;
        private Payment payment;
        private Point point;
        private PointHistory pointHistory;

        @BeforeEach
        void setUp(){
            user = mock(User.class);
            payment = mock(Payment.class);
        }

        @Test
        @DisplayName("포인트 충전 시 포인트 충전 내역이 저장된다")
        void success_point_charge(){
            point = mock(Point.class);
            pointHistory = mock(PointHistory.class);
            Integer amount = 1000;

            when(pointService.findByIdOrFail(user)).thenReturn(point);
            when(payment.getAmount()).thenReturn(amount);
            when(pointHistoryRepository.save(any())).thenReturn(pointHistory);

            PointHistory result = paymentService.chargePointWithHistory(user, payment);

            assertAll(() -> {
                verify(point).charge(amount);
                verify(pointHistoryRepository).save(any());
                assertEquals(result, pointHistory);
            });
        }

        @Test
        @DisplayName("포인트 조회 실패 시 에외가 발생한다")
        void fail_when_pointNotFound(){
            when(pointService.findByIdOrFail(user))
                    .thenThrow(new BusinessException(ErrorCode.POINT_INFO_NOT_FOUND));

            assertThatThrownBy(() -> paymentService.chargePointWithHistory(user, payment))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAIL_TO_POINT_CHARGE);

            verify(pointHistoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("포인트 충전 내역 저장 실패 시 예외가 발생한다")
        void rollback_when_fail_to_save_pointHistory(){
            point = mock(Point.class);
            Integer amount = 1000;

            ArgumentCaptor<Integer> amountCaptor = ArgumentCaptor.forClass(Integer.class);
            when(pointService.findByIdOrFail(user)).thenReturn(point);
            when(payment.getAmount()).thenReturn(amount);
            when(pointHistoryRepository.save(any())).thenThrow(new RuntimeException("저장 실패"));
            when(point.getAmount()).thenReturn(amount);

            assertThatThrownBy(() -> paymentService.chargePointWithHistory(user, payment))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAIL_TO_POINT_CHARGE);

            assertAll(() -> {
                verify(point).charge(amountCaptor.capture());
                assertEquals(amountCaptor.getValue(), amount);
                verify(point).getAmount();
                verifyNoMoreInteractions(point);
            });
        }

        @Test
        @DisplayName("포인트 충전 실패 시 충전 내역이 저장되지 않고 예외가 발생한다")
        void do_not_save_pointHistory_and_throws_exception_when_fail_to_point_charge(){
            point = mock(Point.class);
            when(pointService.findByIdOrFail(user)).thenReturn(point);
            doThrow(new RuntimeException("포인트 충전 실패")).when(point).charge(anyInt());

            assertThatThrownBy(() -> paymentService.chargePointWithHistory(user, payment))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAIL_TO_POINT_CHARGE);

            verifyNoInteractions(pointHistoryRepository);
        }
    }

    @Nested
    @DisplayName("결제 정보 저장 테스트")
    class savePaymentInfo{
        private PaymentReqDto paymentReqDto;
        private TossPaymentRespDto tossPaymentRespDto;
        private User user;
        private Payment payment;

        @BeforeEach
        void setUp(){
            user = mock(User.class);
            payment = mock(Payment.class);
            paymentReqDto = mock(PaymentReqDto.class);
            tossPaymentRespDto = mock(TossPaymentRespDto.class);
        }

        @Test
        @DisplayName("결제 정보 저장에 성공한다")
        void success_save_payment(){
            when(paymentRepository.save(any())).thenReturn(payment);

            Payment result = paymentService.savePaymentInfo(paymentReqDto, tossPaymentRespDto, user);

            assertThat(result).isEqualTo(payment);
            verify(paymentRepository).save(any());
        }

        @Test
        @DisplayName("결제 정보 저장 실패 시 결제 취소 후 예외가 발생한다")
        void fail_to_save_payment_then_cancel_and_throws_exception(){
            when(paymentRepository.save(any())).thenThrow(new RuntimeException("Payment 저장 실패"));
            TossPaymentRespDto canceledPaymentInfo = mock(TossPaymentRespDto.class);
            when(canceledPaymentInfo.getStatus()).thenReturn(PaymentStatus.CANCELED.toString());
            when(canceledPaymentInfo.getCancels()).thenReturn(List.of(Cancels.builder().cancelStatus(PaymentStatus.DONE.toString()).build()));
            when(tossPaymentClient.cancelPayment(any(), any(), any())).thenReturn(canceledPaymentInfo);

            assertThatThrownBy(() -> paymentService.savePaymentInfo(paymentReqDto, tossPaymentRespDto, user))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAIL_TO_SAVE_PAYMENT);

            verify(tossPaymentClient).cancelPayment(any(), any(), any());
        }

        @Test
        @DisplayName("결제 저장 실패 후 결제 취소에서 오류가 나면 예외가 발생한다")
        void fail_to_cancelPayment_then_throws_exception(){
            when(paymentRepository.save(any())).thenThrow(new RuntimeException("Payment 저장 실패"));
            TossPaymentRespDto canceledPaymentInfo = mock(TossPaymentRespDto.class);
            when(canceledPaymentInfo.getStatus()).thenReturn(PaymentStatus.ABORTED.toString());
            when(tossPaymentClient.cancelPayment(any(), any(), any())).thenReturn(canceledPaymentInfo);

            assertThatThrownBy(() -> paymentService.savePaymentInfo(paymentReqDto, tossPaymentRespDto, user))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_CANCEL_INCOMPLETE);

            verify(tossPaymentClient).cancelPayment(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("유저 및 결제 정보 검증 테스트")
    class validateUserAndPayment{
        private User user;
        private PaymentReqDto paymentReqDto;
        private Long userId = 1L;

        private String paymentKey = "paymentKey";
        private String orderId = "orderId";
        private Integer amount = 10000;

        @Test
        @DisplayName("유저와 결제 정보 검증에 성공한다")
        void success_validate(){
            user = mock(User.class);
            paymentReqDto = mock(PaymentReqDto.class);
            when(paymentReqDto.getUserId()).thenReturn(userId);
            when(paymentReqDto.getPaymentKey()).thenReturn(paymentKey);
            when(paymentReqDto.getOrderId()).thenReturn(orderId);
            when(paymentReqDto.getAmount()).thenReturn(amount);
            when(userService.findByIdOrFail(userId)).thenReturn(user);

            TossPaymentRespDto shouldBeCheck = mock(TossPaymentRespDto.class);
            when(shouldBeCheck.getTotalAmount()).thenReturn(amount);
            when(shouldBeCheck.getPaymentKey()).thenReturn(paymentKey);
            when(shouldBeCheck.getOrderId()).thenReturn(orderId);
            when(tossPaymentClient.checkPaymentInfo(any(), any())).thenReturn(shouldBeCheck);

            User result = paymentService.validateUserAndPayment(paymentReqDto);

            assertEquals(result, user);
            verify(tossPaymentClient).checkPaymentInfo(any(), any());
        }

        @Test
        @DisplayName("유저 검증에 실패하면 예외가 발생한다")
        void fail_when_userNotFound_then_throws_exception(){
            when(userService.findByIdOrFail(any())).thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));
            paymentReqDto = mock(PaymentReqDto.class);

            assertThatThrownBy(() -> paymentService.validateUserAndPayment(paymentReqDto))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

            verify(tossPaymentClient, never()).checkPaymentInfo(any(), any());
        }

        @Test
        @DisplayName("결제 정보 검증에 실패하면 예외가 발생한다")
        void fail_when_payment_info_invalid_then_throws_exception(){
            user = mock(User.class);
            paymentReqDto = mock(PaymentReqDto.class);
            when(paymentReqDto.getUserId()).thenReturn(userId);
            when(paymentReqDto.getPaymentKey()).thenReturn(paymentKey);
            when(paymentReqDto.getOrderId()).thenReturn(orderId);
            when(paymentReqDto.getAmount()).thenReturn(amount);
            when(userService.findByIdOrFail(userId)).thenReturn(user);

            TossPaymentRespDto shouldBeCheck = mock(TossPaymentRespDto.class);
            when(shouldBeCheck.getTotalAmount()).thenReturn(amount+10000);
            when(shouldBeCheck.getPaymentKey()).thenReturn(paymentKey);
            when(shouldBeCheck.getOrderId()).thenReturn(orderId);
            when(tossPaymentClient.checkPaymentInfo(any(), any())).thenReturn(shouldBeCheck);

            assertThatThrownBy(() -> paymentService.validateUserAndPayment(paymentReqDto))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_INFO_MISMATCH);

            verify(tossPaymentClient).checkPaymentInfo(any(), any());
        }
    }

}