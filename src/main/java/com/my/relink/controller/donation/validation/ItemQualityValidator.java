package com.my.relink.controller.donation.validation;

import com.my.relink.domain.item.donation.ItemQuality;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ItemQualityValidator implements ConstraintValidator<ValidItemQuality, String> {

    @Override
    public void initialize(ValidItemQuality constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            ItemQuality.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
