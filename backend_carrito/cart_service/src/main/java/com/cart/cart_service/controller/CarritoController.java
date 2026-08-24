package com.cart.cart_service.controller;

import com.cart.cart_service.model.Carrito;
import com.cart.cart_service.model.CarritoItem;
import com.cart.cart_service.service.CarritoService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/carrito")
@Validated
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @PostMapping("/agregar")
    public ResponseEntity<?> addItem(Authentication auth,
            @RequestParam @Positive(message = "articuloId debe ser positivo") Long articuloId,
            @RequestParam @Positive(message = "cantidad debe ser positiva") int cantidad) {
        String usuarioEmail = auth.getName();
        carritoService.agregarItem(usuarioEmail, articuloId, cantidad);
        return ResponseEntity.ok(Collections.singletonMap("message", "Artículo agregado al carrito"));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeItem(Authentication auth,
            @RequestParam @Positive(message = "itemId debe ser positivo") Long itemId) {
        String usuarioEmail = auth.getName();
        carritoService.eliminarItemDelUsuario(usuarioEmail, itemId);
        return ResponseEntity.ok(Collections.singletonMap("message", "Artículo eliminado del carrito"));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestHeader("Authorization") String authHeader,
            @RequestParam @NotBlank(message = "nuevaDireccionEnvio es obligatoria") String nuevaDireccionEnvio,
            Authentication auth) {
        String token = authHeader.replace("Bearer ", "");
        String usuarioEmail = auth.getName();
        carritoService.confirmarPedido(usuarioEmail, nuevaDireccionEnvio, token);
        return ResponseEntity.ok(Collections.singletonMap("message", "Carrito confirmado y orden enviada"));
    }

    @GetMapping("/pedidos")
    public ResponseEntity<List<Carrito>> pedidosConfirmados(Authentication auth) {
        String usuarioEmail = auth.getName();
        List<Carrito> pedidos = carritoService.obtenerPedidosConfirmados(usuarioEmail);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/items")
    public ResponseEntity<List<CarritoItem>> verItems(Authentication auth) {
        String usuarioEmail = auth.getName();
        List<CarritoItem> items = carritoService.verItems(usuarioEmail);
        return ResponseEntity.ok(items);
    }

}
