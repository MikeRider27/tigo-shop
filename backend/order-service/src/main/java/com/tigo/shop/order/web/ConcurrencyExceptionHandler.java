package com.tigo.shop.order.web;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** La orden cambió mientras se procesaba la petición (p. ej. avanzó de estado): 409 para que el cliente reintente. */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ConcurrencyExceptionHandler {

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handle(OptimisticLockingFailureException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "La orden fue modificada por otro proceso. Actualice e intente nuevamente.");
    }
}
