package com.tigo.shop.order.web;

import com.tigo.shop.common.security.AuthUser;
import com.tigo.shop.order.dto.OrderDtos.CheckoutRequest;
import com.tigo.shop.order.dto.OrderDtos.OrderResponse;
import com.tigo.shop.order.dto.OrderDtos.OrderSummaryResponse;
import com.tigo.shop.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** Confirma el pedido con el contenido del carrito. 201 + Location; 400 carrito vacío; 409 sin stock. */
    @PostMapping
    public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal AuthUser user,
                                                  @Valid @RequestBody CheckoutRequest request) {
        OrderResponse order = orderService.checkout(user.id(), request.shippingAddress());
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(order.id()).toUri();
        return ResponseEntity.created(location).body(order);
    }

    @GetMapping
    public List<OrderSummaryResponse> list(@AuthenticationPrincipal AuthUser user) {
        return orderService.listOrders(user.id());
    }

    @GetMapping("/{id}")
    public OrderResponse get(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return orderService.getOrder(user.id(), id);
    }

    /** Cambio de estado parcial del recurso: PATCH. 409 si la orden ya no se puede cancelar. */
    @PatchMapping("/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return orderService.cancel(user.id(), id);
    }
}
