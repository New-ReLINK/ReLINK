package com.my.relink.client.tosspayments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.relink.client.tosspayments.dto.request.TossPaymentCancelReqDto;
import com.my.relink.client.tosspayments.dto.request.TossPaymentReqDto;
import com.my.relink.client.tosspayments.dto.response.TossPaymentErrorRespDto;
import com.my.relink.client.tosspayments.dto.response.TossPaymentRespDto;
import com.my.relink.client.tosspayments.feature.PaymentFeature;
import com.my.relink.client.tosspayments.ex.TossPaymentErrorCode;
import com.my.relink.client.tosspayments.ex.TossPaymentException;
import com.my.relink.client.tosspayments.ex.TossPaymentNetworkException;
import com.my.relink.client.tosspayments.ex.badRequest.TossPaymentBadRequestException;
import com.my.relink.client.tosspayments.ex.forbidden.TossPaymentForbiddenException;
import com.my.relink.client.tosspayments.ex.notFound.TossPaymentNotFoundException;
import com.my.relink.client.tosspayments.ex.serverError.TossPaymentServerException;
import com.my.relink.client.tosspayments.ex.unAuthorized.TossPaymentUnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
@Service
@Slf4j
public class TossPaymentClient {
    private final RestClient restClient;
    private final ObjectMapper om;
    private static final String ERROR_LOG_FORMAT = "[토스페이먼츠 %s 실패] paymentKey={}, message={}";
    private static final String SYSTEM_ERROR_LOG_FORMAT = "[토스페이먼츠 %s 중 예상치 못한 예외 발생] paymentKey={}";



    @Retryable(
            retryFor = {TossPaymentNetworkException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000),
            noRetryFor = {TossPaymentException.class},
            recover = "recoverPaymentForCancelPayment"
    )
    public TossPaymentRespDto cancelPayment(String paymentKey, TossPaymentCancelReqDto cancelReqDto, PaymentFeature paymentFeature){
        try {
            TossPaymentRespDto response = restClient.post()
                    .uri(TossPaymentApiPath.PAYMENT_CANCEL, paymentKey)
                    .body(cancelReqDto)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, ((request, errorResponse) -> {
                        TossPaymentErrorRespDto tossPaymentErrorRespDto = parseErrorResponse(errorResponse);
                        TossPaymentErrorCode errorCode = tossPaymentErrorRespDto.getErrorCode();
                        handleError(paymentKey, errorResponse, errorCode);
                    }))
                    .body(TossPaymentRespDto.class);

            log.info("[토스페이먼츠 결제 취소 성공] paymentKey={}, orderId={}, totalAmount={} ",
                    response.getPaymentKey(),
                    response.getOrderId(),
                    response.getTotalAmount()
            );
            return response;
        } catch (Exception e){
            checkAndThrowIfRetryable(e);
            throw handlePaymentException(paymentFeature, paymentKey, e);
        }
    }


    @Retryable(
            retryFor = {TossPaymentNetworkException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000),
            noRetryFor = {TossPaymentException.class},
            recover = "recoverPaymentForCheckPaymentInfo"
    )
    public TossPaymentRespDto checkPaymentInfo(String paymentKey, PaymentFeature paymentFeature){
        try {
            TossPaymentRespDto response = restClient.get()
                    .uri(TossPaymentApiPath.PAYMENT_INFO, paymentKey)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, ((request, errorResponse) -> {
                        TossPaymentErrorRespDto tossPaymentErrorRespDto = parseErrorResponse(errorResponse);
                        TossPaymentErrorCode errorCode = tossPaymentErrorRespDto.getErrorCode();
                        handleError(paymentKey, errorResponse, errorCode);
                    }))
                    .body(TossPaymentRespDto.class);
            log.info("[토스페이먼츠 결제 정보 조회 성공] paymentKey={}, orderId={}, totalAmount={}",
                    response.getPaymentKey(),
                    response.getOrderId(),
                    response.getTotalAmount()
            );
            return response;
        } catch (Exception e) {
            checkAndThrowIfRetryable(e);
            throw handlePaymentException(paymentFeature, paymentKey, e);
        }
    }


    @Retryable(
            retryFor = {TossPaymentNetworkException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000),
            noRetryFor = {TossPaymentException.class},
            recover = "recoverPaymentForPaymentConfirm"
    )
    public TossPaymentRespDto confirmPayment(TossPaymentReqDto tossPaymentReqDto, PaymentFeature paymentFeature){
        log.info("[토스페이먼츠 결제 승인 요청] paymentKey={}, orderId={}, amount={}",
                tossPaymentReqDto.getPaymentKey(),
                tossPaymentReqDto.getOrderId(),
                tossPaymentReqDto.getAmount()
        );
        try {
            TossPaymentRespDto response = restClient
                    .post()
                    .uri(TossPaymentApiPath.PAYMENT_CONFIRM)
                    .body(tossPaymentReqDto)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, ((request, errorResponse) -> {
                        TossPaymentErrorRespDto tossPaymentErrorRespDto = parseErrorResponse(errorResponse);
                        TossPaymentErrorCode errorCode = tossPaymentErrorRespDto.getErrorCode();
                        String paymentKey = tossPaymentErrorRespDto.getPaymentKey();
                        handleError(paymentKey, errorResponse, errorCode);
                    }))
                    .body(TossPaymentRespDto.class);

            log.info("[토스페이먼츠 결제 승인 성공] paymentKey={}, orderId={}, totalAmount={}",
                    response.getPaymentKey(),
                    response.getOrderId(),
                    response.getTotalAmount()
            );
            return response;
        } catch (Exception e){
            checkAndThrowIfRetryable(e);
            throw handlePaymentException(paymentFeature, tossPaymentReqDto.getPaymentKey(), e);
        }
    }


    private void checkAndThrowIfRetryable(Exception e){
        if (e instanceof TossPaymentException) {
            throw (TossPaymentException) e;
        }

        if (TossPaymentNetworkException.isRetryableException(e)) {
            throw new TossPaymentNetworkException("네트워크 통신 오류가 발생했습니다", e);
        }
    }


    private TossPaymentException handlePaymentException(PaymentFeature feature, String paymentKey, Exception e) {
        if (e instanceof TossPaymentException) {
            log.warn(ERROR_LOG_FORMAT.formatted(feature.getDescription()), paymentKey, ((TossPaymentException) e).getErrorCode().getMessage(), e);
            return (TossPaymentException) e;
        }
        log.error(SYSTEM_ERROR_LOG_FORMAT.formatted(feature.getDescription()), paymentKey, e);
        return new TossPaymentException(TossPaymentErrorCode.UNKNOWN_ERROR, paymentKey, feature.getDescription() + " 중 예상치 못한 오류가 발생했습니다.");
    }


    private void handleError(String paymentKey, ClientHttpResponse errorResponse, TossPaymentErrorCode errorCode) throws IOException {
        switch(errorResponse.getStatusCode().value()){
            case 400 -> throw new TossPaymentBadRequestException(errorCode, paymentKey);
            case 401 -> throw new TossPaymentUnauthorizedException(errorCode, paymentKey);
            case 403 -> throw new TossPaymentForbiddenException(errorCode, paymentKey);
            case 404 -> throw new TossPaymentNotFoundException(errorCode, paymentKey);
            case 500 -> throw new TossPaymentServerException(errorCode, paymentKey);
            default -> throw new TossPaymentServerException(
                    TossPaymentErrorCode.UNKNOWN_ERROR,
                    paymentKey
            );
        }
    }


    private void logAndThrowRecoveryFailure(String paymentKey, PaymentFeature paymentFeature, TossPaymentNetworkException e) {
        log.error("[토스페이먼츠 {} 실패] 최대 재시도 횟수 초과. paymentKey={}",
                paymentFeature.getDescription(),
                paymentKey,
                e
        );
        throw new TossPaymentException(
                TossPaymentErrorCode.NETWORK_ERROR,
                paymentKey,
                paymentFeature.getDescription() + " 중 네트워크 오류가 발생했습니다"
        );
    }


    @Recover
    public TossPaymentRespDto recoverPaymentForCancelPayment(TossPaymentNetworkException e, String paymentKey, TossPaymentCancelReqDto cancelReqDto, PaymentFeature paymentFeature) {
        logAndThrowRecoveryFailure(paymentKey, paymentFeature, e);
        return null;
    }


    @Recover
    public TossPaymentRespDto recoverPaymentForCheckPaymentInfo(TossPaymentNetworkException e, String paymentKey, PaymentFeature paymentFeature) {
        logAndThrowRecoveryFailure(paymentKey, paymentFeature, e);
        return null;
    }


    @Recover
    public TossPaymentRespDto recoverPaymentForPaymentConfirm(TossPaymentNetworkException e, TossPaymentReqDto tossPaymentReqDto, PaymentFeature paymentFeature) {
        logAndThrowRecoveryFailure(tossPaymentReqDto.getPaymentKey(), paymentFeature, e);
        return null;
    }


    private TossPaymentErrorRespDto parseErrorResponse(ClientHttpResponse response) throws IOException {
        try (InputStream body = response.getBody()) {
            return om.readValue(body, TossPaymentErrorRespDto.class);
        }
    }

}
