package com.mongodbmodfactory.kitchensink_boot.member;

import jakarta.validation.ConstraintViolation;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Set;

public class MemberValidator implements Validator {
    private final jakarta.validation.Validator jakartaValidator;

    public MemberValidator(jakarta.validation.Validator jakartaValidator) {
        this.jakartaValidator = jakartaValidator;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Member.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Set<ConstraintViolation<Object>> results = jakartaValidator.validate(target);
        results.forEach((result) -> errors.rejectValue(result.getPropertyPath().toString(), "", result.getMessage()));
    }
}
