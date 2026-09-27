package com.tigo.shop.order.service;

import com.tigo.shop.common.web.ApiException;
import com.tigo.shop.order.client.CatalogClient;
import com.tigo.shop.order.client.CatalogClient.ProductInfo;
import com.tigo.shop.order.domain.CartItem;
import com.tigo.shop.order.domain.CartItemRepository;
import com.tigo.shop.order.dto.OrderDtos.CartResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.tigo.shop.order.dto.OrderDtos.MAX_QUANTITY_PER_ITEM;

@Service
public class CartService {

    private final CartItemRepository cartItems;
    private final CatalogClient catalog;

    public CartService(CartItemRepository cartItems, CatalogClient catalog) {
        this.cartItems = cartItems;
        this.catalog = catalog;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        return CartResponse.from(cartItems.findByUserIdOrderByAddedAtAsc(userId));
    }

    /** Agrega un artículo; si ya estaba en el carrito, suma la cantidad. */
    @Transactional
    public CartResponse addItem(Long userId, Long productId, int quantity) {
        ProductInfo product = catalog.getProduct(productId);
        CartItem item = cartItems.findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> new CartItem(userId, productId));
        int newQuantity = item.getQuantity() + quantity;
        validateQuantity(product, newQuantity);
        item.setQuantity(newQuantity);
        item.refreshSnapshot(product.name(), product.price(), product.imageUrl());
        cartItems.save(item);
        return getCart(userId);
    }

    @Transactional
    public CartResponse updateQuantity(Long userId, Long productId, int quantity) {
        CartItem item = findItem(userId, productId);
        ProductInfo product = catalog.getProduct(productId);
        validateQuantity(product, quantity);
        item.setQuantity(quantity);
        item.refreshSnapshot(product.name(), product.price(), product.imageUrl());
        return getCart(userId);
    }

    @Transactional
    public void removeItem(Long userId, Long productId) {
        cartItems.delete(findItem(userId, productId));
    }

    @Transactional
    public void clear(Long userId) {
        cartItems.deleteAllByUserId(userId);
    }

    private CartItem findItem(Long userId, Long productId) {
        return cartItems.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> ApiException.notFound("El artículo no está en el carrito"));
    }

    private static void validateQuantity(ProductInfo product, int quantity) {
        if (product.stock() <= 0) {
            throw ApiException.conflict("\"" + product.name() + "\" está agotado");
        }
        if (quantity > product.stock()) {
            throw ApiException.conflict("Solo hay " + product.stock() + " unidades disponibles de \""
                    + product.name() + "\"");
        }
        if (quantity > MAX_QUANTITY_PER_ITEM) {
            throw ApiException.badRequest("La cantidad máxima por artículo es " + MAX_QUANTITY_PER_ITEM);
        }
    }
}
