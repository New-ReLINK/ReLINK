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
import com.my.relink.service.payment.ex.PaymentCancelFailException;
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


    /**
     * 포인트 충전 및 이력을 생성하는 메서드
     * 실패 시 모든 변경사항이 롤백됩니다
     *
     * @param user 포인트를 충전할 사용자
     * @param payment 충전할 결제 정보
     * @return 생성된 포인트 충전 이력
     * @throws BusinessException 포인트 충전 실패 시
     */
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


    /**
     * 결제 정보의 상태를 취소로 변경하고 취소 사유를 업데이트
     *
     * @param payment 취소할 결제 정보
     * @param canceledPaymentInfo 토스페이먼츠의 취소된 결제 응답 정보
     * @throws BusinessException 결제 상태 업데이트 실패 시
     */
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


    /**
     * 결제 정보를 저장하는 메서드
     * 저장 실패 시 자동으로 결제 취소 프로세스를 진행합니다
     *
     * @param paymentReqDto 결제 요청 정보
     * @param tossPaymentRespDto 토스페이먼츠 결제 응답 정보
     * @param user 결제 사용자 정보
     * @return 저장된 결제 정보
     * @throws BusinessException 결제 정보 저장 실패 시
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment savePaymentInfo(PaymentReqDto paymentReqDto, TossPaymentRespDto tossPaymentRespDto, User user){
        try {
            return savePayment(paymentReqDto, tossPaymentRespDto, user);
        }catch(Exception e){
            log.error("[결제 내역 저장 중 오류 발생] cause = {}, userId = {}", e.getMessage(), user.getId());
            handlePaymentSaveFailure(paymentReqDto, user, e);
            throw new BusinessException(ErrorCode.FAIL_TO_SAVE_PAYMENT);
        }
    }


    /**
     * 결제 저장 실패 시 결제 취소 프로세스를 처리
     * 취소 실패 시 알림 발송 및 예외를 발생시킵니다
     *
     * @param paymentReqDto 결제 요청 정보
     * @param user 사용자 정보
     * @param originalError 원본 예외
     * @throws BusinessException 결제 취소 실패 시
     */
    private void handlePaymentSaveFailure(PaymentReqDto paymentReqDto, User user, Exception originalError){
        log.info("[결제 취소 프로세스 시작] userId = {}", user.getId());
        try{
            processCancelPayment(
                    paymentReqDto,
                    user,
                    PaymentCancelReason.SERVER_ERROR_FAIL_TO_SAVE_PAYMENT);
            log.info("[결제 취소 프로세스 완료] 토스 결제 취소 및 상태 업데이트 성공. userId = {}", user.getId());
        } catch (PaymentCancelFailException e){
           throw new BusinessException(e.getErrorCode());
        } catch (Exception e){
            handlePaymentCancelFailure(paymentReqDto, user, originalError, e);
            throw new BusinessException(ErrorCode.CRITICAL_PAYMENT_SAVE_ERROR);
        }
    }


    /**
     * 결제 취소 요청 및 취소 상태 검증을 수행
     *
     * @param paymentReqDto 결제 요청 정보
     * @param user 사용자 정보
     * @param cancelReason 결제 취소 사유
     * @throws PaymentCancelFailException 결제 취소 실패 시
     * @throws BusinessException 결제 취소 상태 검증 실패 시
     */
    private void processCancelPayment(PaymentReqDto paymentReqDto, User user, PaymentCancelReason cancelReason) {
        TossPaymentRespDto canceledPaymentInfo = cancelPaymentWithReason(
                paymentReqDto,
                cancelReason);
        validateCanceledPayment(user, canceledPaymentInfo);
    }


    /**
     * 결제 취소 실패 시 알림 처리를 담당
     *
     * @param paymentReqDto 결제 요청 정보
     * @param user 사용자 정보
     * @param originalError 원본 예외
     * @param cancelError 취소 시 발생한 예외
     */
    private void handlePaymentCancelFailure(PaymentReqDto paymentReqDto, User user,
                                            Exception originalError, Exception cancelError) {
        log.error("[결제 취소 실패] cause = {}, userId = {}", cancelError.getMessage(), user.getId());
        sendPaymentCancelFailureAlert(paymentReqDto, user, originalError, cancelError);
    }


    /**
     * 결제 정보를 저장
     *
     * @param paymentReqDto 결제 요청 정보
     * @param tossPaymentRespDto 토스페이먼츠 결제 응답 정보
     * @param user 사용자 정보
     * @return 저장된 결제 정보
     */
    private Payment savePayment(PaymentReqDto paymentReqDto, TossPaymentRespDto tossPaymentRespDto, User user) {
        Payment payment = paymentRepository.save(paymentReqDto.toEntity(
                tossPaymentRespDto,
                user,
                PaymentType.POINT_CHARGE));
        log.info("[결제 내역 저장 완료] userId = {}, paymentId = {}", user.getId(), payment.getId());
        return payment;
    }


    /**
     * TODO 구현 예정
     * 결제 취소 실패 시 관리자 알림을 발송
     *
     * @param paymentReqDto 결제 요청 정보
     * @param user 사용자 정보
     * @param originalError 원본 예외
     * @param cancelError 취소 시 발생한 예외
     */
    private void sendPaymentCancelFailureAlert(PaymentReqDto paymentReqDto, User user, Exception originalError, Exception cancelError) {
        String title = String.format("[결제 취소 실패] - merchantUid: %s", paymentReqDto.getOrderId());
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


    /**
     * 사용자 정보 조회 및 결제 정보 검증
     *
     * @param paymentReqDto 결제 요청 정보
     * @return 검증된 사용자 정보
     * @throws BusinessException 사용자 조회 실패 또는 결제 정보 검증 실패 시
     */
    public User validateUserAndPayment(PaymentReqDto paymentReqDto){
        User user = userService.findByIdOrFail(paymentReqDto.getUserId());
        validatePaymentInfo(paymentReqDto);
        return user;
    }


    /**
     * 토스페이먼츠에 결제 취소 요청
     *
     * @param paymentReqDto 결제 요청 정보
     * @param cancelReason 취소 사유
     * @return 토스페이먼츠의 결제 취소 응답 정보
     */
    private TossPaymentRespDto cancelPaymentWithReason(PaymentReqDto paymentReqDto, PaymentCancelReason cancelReason){
        return tossPaymentClient.cancelPayment(
                paymentReqDto.getPaymentKey(),
                new TossPaymentCancelReqDto(cancelReason.getMessage()),
                PaymentFeature.PAYMENT_CANCEL);
    }


    /**
     * 취소된 결제 건에 대한 응답 상태를 검증
     * - status가 CANCELED 상태여야 함
     * - cancelStatus가 DONE 상태여야 함
     *
     * @param user 사용자 정보
     * @param canceledPaymentInfo 취소된 결제 정보
     * @throws PaymentCancelFailException 결제 취소 상태가 유효하지 않을 경우
     * @throws BusinessException 결제 취소 상태 정보를 찾을 수 없는 경우
     */
    private void validateCanceledPayment(User user, TossPaymentRespDto canceledPaymentInfo) {

        if(!canceledPaymentInfo.getStatus().equals(PaymentStatus.CANCELED.toString())){
            log.error("[결제 취소 실패] 결제 상태가 CANCELED가 아님. userId = {}, status = {}", user.getId(), canceledPaymentInfo.getStatus());
            throw new PaymentCancelFailException(ErrorCode.PAYMENT_CANCEL_INCOMPLETE);
        }

        String cancelStatus = canceledPaymentInfo.getCancels().stream()
                .findFirst()
                .map(Cancels::getCancelStatus)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOSS_PAYMENT_CANCEL_STATUS_NOT_FOUND));
        if(!cancelStatus.equals(PaymentStatus.DONE.toString())){
            log.error("[결제 취소 실패] 결제 취소 상태가 DONE이 아님. userId = {}, cancelStatus = {}", user.getId(), cancelStatus);
            throw new PaymentCancelFailException(ErrorCode.PAYMENT_CANCEL_STATUS_INVALID);
        }
    }


    /**
     * 결제 요청 정보와 토스페이먼츠의 결제 정보를 검증
     * amount, paymentKey, orderId 일치 여부를 확인
     *
     * @param paymentReqDto 결제 요청 정보
     * @throws BusinessException 결제 정보가 일치하지 않는 경우
     */
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
