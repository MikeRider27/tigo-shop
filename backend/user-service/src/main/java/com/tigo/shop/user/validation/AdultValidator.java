package com.tigo.shop.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;

public class AdultValidator implements ConstraintValidator<Adult, LocalDate> {

    private final Clock clock;
    private int minAge;

    public AdultValidator() {
        this(Clock.systemDefaultZone());
    }

    AdultValidator(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void initialize(Adult annotation) {
        this.minAge = annotation.minAge();
    }

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        if (birthDate == null) {
            return true; // la obligatoriedad la valida @NotNull
        }
        LocalDate today = LocalDate.now(clock);
        return !birthDate.isAfter(today) && Period.between(birthDate, today).getYears() >= minAge;
    }
}
