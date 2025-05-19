package com.cart.cart_service.controller;

import com.cart.cart_service.model.Carrito;
import com.cart.cart_service.model.CarritoItem;
import com.cart.cart_service.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @PostMapping("/agregar")
    public ResponseEntity<?> addItem(Authentication auth,
            @RequestParam Long articuloId,
            @RequestParam int cantidad) {
        String usuarioEmail = auth.getName();
        carritoService.agregarItem(usuarioEmail, articuloId, cantidad);
        return ResponseEntity.ok("Artículo agregado al carrito");
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeItem(Authentication auth,
            @RequestParam Long itemId) {
        String usuarioEmail = auth.getName();
        carritoService.eliminarItemDelUsuario(usuarioEmail, itemId);
        return ResponseEntity.ok("Artículo eliminado del carrito");
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestHeader("Authorization") String authHeader,
            @RequestParam String nuevaDireccionEnvio,
            Authentication auth) {
        String token = authHeader.replace("Bearer ", "");
        String usuarioEmail = auth.getName();
        carritoService.confirmarPedido(usuarioEmail, nuevaDireccionEnvio, token);
        return ResponseEntity.ok("Carrito confirmado y orden enviada");
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
