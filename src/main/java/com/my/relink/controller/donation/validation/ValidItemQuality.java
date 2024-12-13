package com.my.relink.controller.donation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ItemQualityValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidItemQuality {
    String message() default "상품의 상태를 잘 못 입력하셨습니다. NEW, USED, DEFECTIVE 중 한개를 선택해주세요. ";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
