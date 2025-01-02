package com.my.relink.handler;

import com.my.relink.client.tosspayments.ex.TossPaymentException;
import com.my.relink.client.tosspayments.ex.badRequest.TossPaymentBadRequestException;
import com.my.relink.client.tosspayments.ex.forbidden.TossPaymentForbiddenException;
import com.my.relink.client.tosspayments.ex.notFound.TossPaymentNotFoundException;
import com.my.relink.client.tosspayments.ex.serverError.TossPaymentServerException;
import com.my.relink.client.tosspayments.ex.unAuthorized.TossPaymentUnauthorizedException;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.util.api.ApiResult;
import io.sentry.Sentry;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 예상치 못한 Exception 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<String>> handleGeneralException(Exception e){
        log.error("예기치 못한 내부 오류 발생: {}", e.getMessage(), e);
        // Sentry로 예외 전송
        Sentry.configureScope(scope -> {
            scope.setTag("alertType", "GENERAL_EXCEPTION");
        });
        Sentry.captureException(e);
        return new ResponseEntity<>(
                ApiResult.error(ErrorCode.UNEXPECTED_SERVER_ERROR),
                HttpStatus.valueOf(ErrorCode.UNEXPECTED_SERVER_ERROR.getStatus())
        );
    }

    /**
     * 비즈니스에서 발생한 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<String>> handleBusinessException(BusinessException e){
        // Sentry로 예외 전송
        Sentry.captureException(e);

        return new ResponseEntity<>(
                ApiResult.error(e.getErrorCode()),
                HttpStatus.valueOf(e.getErrorCode().getStatus())
        );
    }


    /**
     * 토스 페이먼츠 에러 처리
     */
    @ExceptionHandler(TossPaymentException.class)
    public ResponseEntity<ApiResult<String>> handleTossPaymentException(TossPaymentException e) {
        // Sentry로 예외 전송
        Sentry.captureException(e);

        HttpStatus status = getHttpStatusForException(e);
        return new ResponseEntity<>(
                ApiResult.error(e.getMessage(), status.value()),
                status
        );
    }


    /**
     * DTO 유효성 검사 실패 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Map<String, String>>> handleMethodArgumentValidationException(MethodArgumentNotValidException e){
        Map<String, String> errorMap = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
            errorMap.put(error.getField(), error.getDefaultMessage())
        );
        // Sentry로 예외 전송
        Sentry.captureException(e);

        return new ResponseEntity<>(
                ApiResult.error(errorMap, ErrorCode.VALIDATION_FAILED),
                HttpStatus.valueOf(ErrorCode.VALIDATION_FAILED.getStatus())
        );
    }

    /**
     * 쿼리 파라미터, path variable 유효성 검사 실패 예외 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Map<String, String>>> handleConstraintViolationException(ConstraintViolationException e) {
        Map<String, String> errorMap = new HashMap<>();
        e.getConstraintViolations().forEach(error ->
                errorMap.put(extractFieldName(error.getPropertyPath()), error.getMessage()));
        // Sentry로 예외 전송
        Sentry.captureException(e);
        return new ResponseEntity<>(
                ApiResult.error(errorMap, ErrorCode.VALIDATION_FAILED),
                HttpStatus.valueOf(ErrorCode.VALIDATION_FAILED.getStatus())
        );
    }

    /**
     * 요청한 리소스를 찾을 수 없는 경우 예외 처리
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResult<String>> handleNoResourceFoundException(NoResourceFoundException e) {
        // Sentry로 예외 전송
        Sentry.captureException(e);
        return new ResponseEntity<>(
                ApiResult.error(ErrorCode.RESOURCE_NOT_FOUND),
                HttpStatus.valueOf(ErrorCode.RESOURCE_NOT_FOUND.getStatus())
        );
    }

    /**
     *
     * @param propertyPath
     * @return 속성 경로에서 필드명 추출
     * ex) order.email -> email
     */
    private String extractFieldName(Path propertyPath){
        Path.Node lastNode = null;
        for (Path.Node node : propertyPath) {
            lastNode = node;
        }
        return lastNode != null? lastNode.getName(): "필드 정보 없음";
    }

    private HttpStatus getHttpStatusForException(TossPaymentException e) {
        HttpStatus status;
        if (e instanceof TossPaymentBadRequestException) {
            status = HttpStatus.BAD_REQUEST;
            //return HttpStatus.BAD_REQUEST;
        } else if (e instanceof TossPaymentUnauthorizedException) {
            status = HttpStatus.UNAUTHORIZED;
            //return HttpStatus.UNAUTHORIZED;
        } else if (e instanceof TossPaymentForbiddenException) {
            status = HttpStatus.FORBIDDEN;
            //return HttpStatus.FORBIDDEN;
        } else if (e instanceof TossPaymentNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            //return HttpStatus.NOT_FOUND;
        } else if (e instanceof TossPaymentServerException) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            //return HttpStatus.INTERNAL_SERVER_ERROR;
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            //return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        if(status.is5xxServerError()){
            Sentry.configureScope(scope -> scope.setTag("alertType", "SERVER_ERROR"));
        }

        return status;
    }


}
