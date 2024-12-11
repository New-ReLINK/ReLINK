package com.my.relink.handler;

import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.util.api.ApiResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.client.MockMvcHttpConnector;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class GlobalExceptionHandlerTest {


    private GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("예상치 못한 Exception 처리 테스트")
    void handleGeneralException(){
        Exception ex = new RuntimeException("에러");

        ResponseEntity<ApiResult<String>> response = handler.handleGeneralException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.valueOf(ErrorCode.UNEXPECTED_SERVER_ERROR.getStatus()));
        assertThat(response.getBody().getError().getMessage()).isEqualTo(ErrorCode.UNEXPECTED_SERVER_ERROR.getMessage());
    }

    @Test
    @DisplayName("비즈니스 에러 처리 테스트")
    void handleBusinessException(){
        BusinessException ex = new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);

        ResponseEntity<ApiResult<String>> response = handler.handleBusinessException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.valueOf(ErrorCode.RESOURCE_NOT_FOUND.getStatus()));
        assertThat(response.getBody().getError().getMessage()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("DTO 유효성 검사 실패 처리 테스트")
    void handleMethodArgumentValidationException(){
        MethodArgumentNotValidException ex = createMethodArgumentNotValidationEx();

        ResponseEntity<ApiResult<Map<String, String>>> response = handler.handleMethodArgumentValidationException(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(ErrorCode.VALIDATION_FAILED.getStatus());
        assertThat(response.getBody().getError().getMessage()).isEqualTo(ErrorCode.VALIDATION_FAILED.getMessage());

        Map<String, String> errorMap = response.getBody().getData();
        assertThat(errorMap)
                .containsEntry("email", "이메일 형식이 올바르지 않습니다")
                .hasSize(1);
    }

    @Test
    @DisplayName("쿼리 파라미터/패스 변수 유효성 검사 실패 처리 테스트")
    void handleConstraintViolationException() {

        ConstraintViolationException exception = createConstraintViolationEx();

        ResponseEntity<ApiResult<Map<String, String>>> response =
                handler.handleConstraintViolationException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(ErrorCode.VALIDATION_FAILED.getStatus());
        assertThat(response.getBody().getError().getMessage()).isEqualTo(ErrorCode.VALIDATION_FAILED.getMessage());

        Map<String, String> errorMap = response.getBody().getData();
        assertThat(errorMap)
                .containsEntry("page", "페이지 번호는 0 이상이어야 함")
                .hasSize(1);
    }

    @Test
    @DisplayName("리소스를 찾을 수 없는 경우 처리 테스트")
    void handleNoResourceFoundException(){
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, null);

        ResponseEntity<ApiResult<String>> response = handler.handleNoResourceFoundException(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND.getStatus());
        assertThat(response.getBody().getError().getMessage()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND.getMessage());
    }


    private ConstraintViolationException createConstraintViolationEx(){
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(MockConstraintViolation.of("page", "페이지 번호는 0 이상이어야 함"));

        return new ConstraintViolationException("유효성 검사 실패", violations);
    }

    private MethodArgumentNotValidException createMethodArgumentNotValidationEx(){
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(new FieldError("object", "email", "이메일 형식이 올바르지 않습니다"));

        return new MethodArgumentNotValidException(
                MockHttpMessageConverter.mock().getMockMethod(),
                bindingResult
        );
    }

    static class MockConstraintViolation{
        public static ConstraintViolation<?> of (String propertyPath, String message){
            ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
            Path path = mock(Path.class);
            Path.Node node = mock(Path.Node.class);

            when(violation.getPropertyPath()).thenReturn(path);
            when(violation.getMessage()).thenReturn(message);
            when(path.iterator()).thenReturn(Collections.singletonList(node).iterator());
            when(node.getName()).thenReturn(propertyPath);

            return violation;
        }
    }

    static class MockHttpMessageConverter {
        public static MockHttpMessageConverter mock() {
            return new MockHttpMessageConverter();
        }

        public MethodParameter getMockMethod() {
            try {
                Method method = this.getClass().getMethod("mock");
                return new MethodParameter(method, -1);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

}