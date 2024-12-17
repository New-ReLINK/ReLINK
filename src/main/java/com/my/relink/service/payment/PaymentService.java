package com.my.relink.service.payment;

import com.my.relink.client.tosspayments.TossPaymentClient;
import com.my.relink.client.tosspayments.dto.request.TossPaymentCancelReqDto;
import com.my.relink.client.tosspayments.dto.request.TossPaymentReqDto;
import com.my.relink.client.tosspayments.dto.response.TossPaymentRespDto;
import com.my.relink.client.tosspayments.feature.PaymentFeature;
import com.my.relink.client.tosspayments.feature.PaymentStatus;
import com.my.relink.controller.payment.dto.request.PaymentReqDto;
import com.my.relink.domain.payment.Payment;
import com.my.relink.domain.payment.PaymentCancelReason;
import com.my.relink.domain.payment.PaymentType;
import com.my.relink.domain.payment.repository.PaymentRepository;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.pointHistory.PointHistory;
import com.my.relink.domain.point.pointHistory.repository.PointHistoryRepository;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.service.PointService;
import com.my.relink.service.UserService;
import com.my.relink.service.payment.dto.PaymentValidation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.my.relink.client.tosspayments.dto.response.TossPaymentRespDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PaymentService {

    private final TossPaymentClient tossPaymentClient;
    private final UserService userService;
    private final PaymentRepository paymentRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PointService pointService;
    // TODO 구현 예정.  private final AlertService alertService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PointHistory chargePointWithHistory(User user, Payment payment){
        try {
            Point point = pointService.findByIdOrFail(user);
            point.charge(payment.getAmount());
            PointHistory pointHistory = pointHistoryRepository.save(PointHistory.createChargeHistory(point));

            log.info("[포인트 충전 프로세스 완료] userId = {}, pointId = {}", user.getId(), point.getId());
            return pointHistory;
        } catch (Exception e) {
            log.error("[포인트 충전 프로세스 실패] cause = {}, userId = {}", e.getMessage(), user.getId());
            throw new BusinessException(ErrorCode.FAIL_TO_POINT_CHARGE);
        }
    }

    //payment 상태를 취소로 바꾸고 취소 사유를 기재한다
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePaymentStatusToCanceled(Payment payment, TossPaymentRespDto canceledPaymentInfo) {
        try {
            payment.updateFailInfo(
                    PaymentCancelReason.SERVER_ERROR_FAIL_TO_UPDATE_POINT.getMessage(),
                    canceledPaymentInfo.getStatus());
        }catch(Exception e){
            log.error("[결제 내역 취소 상태로 변경 중 오류 발생] cause = {}, paymentId = {}, paymentKey = {}", e.getMessage(), payment.getId(), canceledPaymentInfo.getPaymentKey());
            throw new BusinessException(ErrorCode.FAIL_TO_UPDATE_PAYMENT_STATUS);
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment savePaymentInfo(PaymentReqDto paymentReqDto, TossPaymentRespDto tossPaymentRespDto, User user){
        Payment payment = null;
        try {
            payment = paymentRepository.save(paymentReqDto.toEntity(
                    tossPaymentRespDto,
                    user,
                    PaymentType.POINT_CHARGE));
            log.info("[결제 내역 저장 완료] userId = {}, paymentId = {}", user.getId(), payment.getId());
            return payment;
        }catch(Exception e){
            log.error("[결제 내역 저장 중 오류 발생] cause = {}, userId = {}", e.getMessage(), user.getId());
            try {
                log.info("[결제 취소 프로세스 시작] userId = {}", user.getId());
                cancelPaymentAndThrowWhenFailToSavePayment(paymentReqDto, user, e);
                log.info("[결제 취소 프로세스 완료] 토스 결제 취소 및 상태 업데이트 성공. userId = {}, paymentId = {}", user.getId(), payment.getId());
                throw new BusinessException(ErrorCode.FAIL_TO_SAVE_PAYMENT);
            } catch (Exception cancelException) {
                log.error("[심각] 결제 취소 실패 - 수동 개입 필요. 원인: {}", cancelException.getMessage());
                sendPaymentCancelFailureAlert(paymentReqDto, user, e, cancelException);
                throw new BusinessException(ErrorCode.CRITICAL_PAYMENT_SAVE_ERROR);
            }
        }
    }

    private void sendPaymentCancelFailureAlert(PaymentReqDto paymentReqDto, User user, Exception originalError, Exception cancelError) {
        String title = String.format("[긴급] 결제 취소 실패 - merchantUid: %s", paymentReqDto.getOrderId());
        String detailedLog = String.format("""
                    결제취소실패 - 수동 개입 필요
                    시간: %s
                    주문번호: %s
                    결제키: %s
                    사용자 ID: %d
                    결제금액: %d
                    최초에러: %s
                    취소실패원인: %s
                    """,
                LocalDateTime.now(),
                paymentReqDto.getOrderId(),
                paymentReqDto.getPaymentKey(),
                user.getId(),
                paymentReqDto.getAmount(),
                originalError.getMessage(),
                cancelError.getMessage()
        );
        // TODO 구현 예정; 알람 보내기 ex)  alertService.sendEmergencyAlert(title, detailedLog);
    }


    public TossPaymentRespDto cancelPaymentAndThrowWhenFailToUpdatePoint(PaymentReqDto paymentReqDto, User user, Exception e){
        //토스페이먼츠 결제 취소 요청
        TossPaymentRespDto canceledPaymentInfo = cancelPaymentWithReason(paymentReqDto, PaymentCancelReason.SERVER_ERROR_FAIL_TO_UPDATE_POINT);

        //결제 취소 상태 다시 확인 -> DONE 이면 결제 취소 정상 수행
        doubleCheckWhenPaymentCanceled(user, e, canceledPaymentInfo);
        return canceledPaymentInfo;
    }

    private void cancelPaymentAndThrowWhenFailToSavePayment(PaymentReqDto paymentReqDto, User user, Exception e){
        //토스페이먼츠 결제 취소 요청
        TossPaymentRespDto canceledPaymentInfo = cancelPaymentWithReason(paymentReqDto, PaymentCancelReason.SERVER_ERROR_FAIL_TO_SAVE_PAYMENT);

        //결제 취소 상태 다시 확인 -> DONE 이면 결제 취소 정상 수행
        doubleCheckWhenPaymentCanceled(user, e, canceledPaymentInfo);
    }


    public User validateUserAndPayment(PaymentReqDto paymentReqDto){
        User user = userService.findByIdOrFail(paymentReqDto.getUserId());
        validatePaymentInfo(paymentReqDto);
        return user;
    }



    private TossPaymentRespDto cancelPaymentWithReason(PaymentReqDto paymentReqDto, PaymentCancelReason cancelReason){
        return tossPaymentClient.cancelPayment(
                paymentReqDto.getPaymentKey(),
                new TossPaymentCancelReqDto(cancelReason.getMessage()),
                PaymentFeature.PAYMENT_CANCEL);
    }


    /**
     * 취소된 결제 건에 대한 응답 상태 검증
     * status == CANCELD여야 한다
     * cancelStatus == DONE이여야 한다
     * @param user
     * @param e
     * @param canceledPaymentInfo
     */
    private void doubleCheckWhenPaymentCanceled(User user, Exception e, TossPaymentRespDto canceledPaymentInfo) {

        if(!canceledPaymentInfo.getStatus().equals(PaymentStatus.CANCELED.toString())){
            log.error("[결제 취소 실패] 결제 상태가 CANCELED가 아님. userId = {}, status = {}", user.getId(), canceledPaymentInfo.getStatus());
            throw new BusinessException(ErrorCode.PAYMENT_CANCEL_INCOMPLETE);
        }

        String cancelStatus = canceledPaymentInfo.getCancels().stream()
                .findFirst()
                .map(Cancels::getCancelStatus)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOSS_PAYMENT_CANCEL_STATUS_NOT_FOUND));
        if(!cancelStatus.equals(PaymentStatus.DONE.toString())){
            log.error("[결제 취소 실패] 결제 취소 상태가 DONE이 아님. userId = {}, cancelStatus = {}", user.getId(), cancelStatus);
            throw new BusinessException(ErrorCode.PAYMENT_CANCEL_STATUS_INVALID);
        }
    }


    private void validatePaymentInfo(PaymentReqDto paymentReqDto){
        TossPaymentRespDto shouldBeCheck = tossPaymentClient.checkPaymentInfo(
                paymentReqDto.getPaymentKey(),
                PaymentFeature.PAYMENT_INQUIRY
        );

        Map<String, PaymentValidation> validations = Map.of(
                TossPaymentReqDto.FIELD_AMOUNT, new PaymentValidation(
                        TossPaymentReqDto.FIELD_AMOUNT,
                        paymentReqDto.getAmount(),
                        shouldBeCheck.getTotalAmount()
                ),
                TossPaymentReqDto.FIELD_PAYMENT_KEY, new PaymentValidation(
                        TossPaymentReqDto.FIELD_PAYMENT_KEY,
                        paymentReqDto.getPaymentKey(),
                        shouldBeCheck.getPaymentKey()
                ),
                TossPaymentReqDto.FIELD_ORDER_ID, new PaymentValidation(
                        TossPaymentReqDto.FIELD_ORDER_ID,
                        paymentReqDto.getOrderId(),
                        shouldBeCheck.getOrderId()
                )
        );

        List<String> validationErrors = validations.values().stream()
                .filter(validation -> !Objects.equals(validation.getActual(), validation.getExpected()))
                .map(validation -> String.format("%s 불일치: expected = %s, actual = %s", validation.getFieldName(), validation.getExpected(), validation.getActual()))
                .toList();

        if(!validationErrors.isEmpty()){
            String errorDetail = String.join(", ", validationErrors);
            log.error("[결제 정보 불일치 발생] {}", errorDetail);
            throw new BusinessException(ErrorCode.PAYMENT_INFO_MISMATCH);
        }

        log.debug("[결제 정보 검증 완료]");
    }

}
