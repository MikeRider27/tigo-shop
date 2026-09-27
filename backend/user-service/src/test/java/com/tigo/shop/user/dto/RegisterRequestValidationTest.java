package com.tigo.shop.user.dto;

import com.tigo.shop.user.dto.UserDtos.RegisterRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestValidationTest {

    private static jakarta.validation.ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    private static RegisterRequest valid() {
        return new RegisterRequest("Ana", "Pérez", "Zona 10, Ciudad", "ana@correo.com",
                LocalDate.now().minusYears(25), "Secreta123");
    }

    private static Set<String> invalidFields(RegisterRequest req) {
        return validator.validate(req).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void validRequestHasNoViolations() {
        assertThat(invalidFields(valid())).isEmpty();
    }

    @Test
    void allFieldsAreMandatory() {
        var req = new RegisterRequest("", " ", "", "", null, "");
        assertThat(invalidFields(req)).containsExactlyInAnyOrder(
                "firstName", "lastName", "shippingAddress", "email", "birthDate", "password");
    }

    @ParameterizedTest
    @ValueSource(strings = {"sin-arroba.com", "a@b", "a@b.c", "a b@correo.com", "@correo.com"})
    void rejectsMalformedEmails(String email) {
        var v = valid();
        var req = new RegisterRequest(v.firstName(), v.lastName(), v.shippingAddress(), email, v.birthDate(), v.password());
        assertThat(invalidFields(req)).containsExactly("email");
    }

    @Test
    void rejectsMinors() {
        var v = valid();
        var req = new RegisterRequest(v.firstName(), v.lastName(), v.shippingAddress(), v.email(),
                LocalDate.now().minusYears(17), v.password());
        assertThat(invalidFields(req)).containsExactly("birthDate");
    }

    @ParameterizedTest
    @ValueSource(strings = {"corta1", "solamenteletras", "12345678"})
    void rejectsWeakPasswords(String password) {
        var v = valid();
        var req = new RegisterRequest(v.firstName(), v.lastName(), v.shippingAddress(), v.email(), v.birthDate(), password);
        assertThat(invalidFields(req)).containsExactly("password");
    }
}
