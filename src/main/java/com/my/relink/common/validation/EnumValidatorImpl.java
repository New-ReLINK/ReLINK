package com.my.relink.common.validation;

import com.my.relink.domain.item.donation.ItemQuality;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class EnumValidatorImpl implements ConstraintValidator<EnumValidator, ItemQuality> {

    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(EnumValidator constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
    }

    @Override
    public boolean isValid(ItemQuality value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null 값은 @NotNull 애너테이션에서 처리
        }
        return Arrays.stream(enumClass.getEnumConstants())
                .anyMatch(e -> e.name().equals(value.name()));
    }
}
