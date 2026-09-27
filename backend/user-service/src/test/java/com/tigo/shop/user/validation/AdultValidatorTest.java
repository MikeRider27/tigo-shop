package com.tigo.shop.user.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdultValidatorTest {

    private final Clock fixedClock = Clock.fixed(
            LocalDate.of(2026, 9, 27).atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    private AdultValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AdultValidator(fixedClock);
        Adult annotation = mock(Adult.class);
        when(annotation.minAge()).thenReturn(18);
        validator.initialize(annotation);
    }

    @Test
    void acceptsPersonWhoTurns18Today() {
        assertThat(validator.isValid(LocalDate.of(2008, 9, 27), null)).isTrue();
    }

    @Test
    void rejectsPersonWhoTurns18Tomorrow() {
        assertThat(validator.isValid(LocalDate.of(2008, 9, 28), null)).isFalse();
    }

    @Test
    void rejectsFutureDates() {
        assertThat(validator.isValid(LocalDate.of(2030, 1, 1), null)).isFalse();
    }

    @Test
    void nullIsDelegatedToNotNull() {
        assertThat(validator.isValid(null, null)).isTrue();
    }
}
