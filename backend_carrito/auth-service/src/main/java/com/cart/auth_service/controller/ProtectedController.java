package com.cart.auth_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/secure")
public class ProtectedController {

    @GetMapping
    public String mensajeProtegido() {
        return "Acceso autorizado. Bienvenido Miguel!";
    }
}
