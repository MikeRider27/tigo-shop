package com.cart.order_service.controller;

import com.cart.order_service.model.Orden;
import com.cart.order_service.service.OrdenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordenes")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @PostMapping
    public ResponseEntity<Orden> crearOrden(@RequestBody Orden orden, Authentication auth) {
        String email = auth.getName(); // desde el token
        orden.setUsuarioEmail(email);
        return ResponseEntity.ok(ordenService.crearOrden(orden));
    }

    @GetMapping
    public ResponseEntity<List<Orden>> obtenerOrdenes(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(ordenService.obtenerOrdenesPorUsuario(email));
    }

    @GetMapping("/{numeroOrden}")
    public ResponseEntity<Orden> obtenerOrdenPorNumero(@PathVariable String numeroOrden) {
        return ResponseEntity.ok(ordenService.obtenerOrdenPorNumero(numeroOrden));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<List<Orden>> obtenerOrdenesDelUsuario(Authentication auth) {
        String email = auth.getName();
        List<Orden> ordenes = ordenService.obtenerOrdenesPorUsuario(email);
        return ResponseEntity.ok(ordenes);
    }

}
