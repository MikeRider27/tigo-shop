package com.tigo.shop.order.web;

import com.tigo.shop.common.security.AuthUser;
import com.tigo.shop.order.dto.OrderDtos.AddCartItemRequest;
import com.tigo.shop.order.dto.OrderDtos.CartResponse;
import com.tigo.shop.order.dto.OrderDtos.UpdateCartItemRequest;
import com.tigo.shop.order.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Carrito del usuario autenticado (uno por usuario, identificado por el JWT). */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse get(@AuthenticationPrincipal AuthUser user) {
        return cartService.getCart(user.id());
    }

    /** 201 con el carrito actualizado; 409 si no hay stock suficiente; 404 si el artículo no existe. */
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addItem(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(user.id(), request.productId(), request.quantity());
    }

    @PutMapping("/items/{productId}")
    public CartResponse updateItem(@AuthenticationPrincipal AuthUser user, @PathVariable Long productId,
                                   @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateQuantity(user.id(), productId, request.quantity());
    }

    @DeleteMapping("/items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@AuthenticationPrincipal AuthUser user, @PathVariable Long productId) {
        cartService.removeItem(user.id(), productId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@AuthenticationPrincipal AuthUser user) {
        cartService.clear(user.id());
    }
}
