package com.cart.cart_service.service;

import com.cart.cart_service.client.CatalogClient;
import com.cart.cart_service.client.OrderClient;
import com.cart.cart_service.exception.CarritoNotFoundException;
import com.cart.cart_service.exception.ItemNotFoundException;
import com.cart.cart_service.exception.ItemNotOwnedException;
import com.cart.cart_service.model.Carrito;
import com.cart.cart_service.model.CarritoItem;
import com.cart.cart_service.repository.CarritoItemRepository;
import com.cart.cart_service.repository.CarritoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepo;
    private final CarritoItemRepository itemRepo;
    private final OrderClient orderClient;
    private final CatalogClient catalogClient;

    public CarritoService(CarritoRepository carritoRepo,
            CarritoItemRepository itemRepo,
            OrderClient orderClient,
            CatalogClient catalogClient) {
        this.carritoRepo = carritoRepo;
        this.itemRepo = itemRepo;
        this.orderClient = orderClient;
        this.catalogClient = catalogClient;
    }

    public Carrito obtenerCarritoActivo(String email) {
        return carritoRepo.findByUsuarioEmailAndConfirmadoFalse(email)
                .orElseGet(() -> {
                    Carrito nuevo = new Carrito();
                    nuevo.setUsuarioEmail(email);
                    nuevo.setDireccionEnvio("");
                    return carritoRepo.save(nuevo);
                });
    }

    public Carrito agregarItem(String email, Long articuloId, int cantidad) {
        Carrito carrito = obtenerCarritoActivo(email);

        // Verificar si el artículo ya está en el carrito
        CarritoItem existente = itemRepo.findByCarritoAndArticuloId(carrito, articuloId);
        if (existente != null) {
            existente.setCantidad(existente.getCantidad() + cantidad);
            itemRepo.save(existente);
        } else {
            CarritoItem item = new CarritoItem();
            item.setArticuloId(articuloId);
            item.setCantidad(cantidad);
            item.setCarrito(carrito);
            itemRepo.save(item);
        }

        return carritoRepo.findById(carrito.getId()).orElseThrow();
    }

    public void eliminarItemDelUsuario(String email, Long itemId) {
        Carrito carrito = obtenerCarritoActivo(email);

        CarritoItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        if (!item.getCarrito().getId().equals(carrito.getId())) {
            throw new ItemNotOwnedException();
        }

        itemRepo.delete(item);
    }

    public Carrito confirmarPedido(String email, String nuevaDireccion, String token) {
        Carrito carrito = obtenerCarritoActivo(email);
        carrito.setDireccionEnvio(nuevaDireccion);
        carrito.setConfirmado(true);
        carritoRepo.save(carrito);
    
        List<CarritoItem> items = itemRepo.findByCarrito(carrito);
        List<Map<String, Object>> itemsPayload = new ArrayList<>();
    
        for (CarritoItem item : items) {
            // ✅ Nuevo llamado con token
            Double precio = catalogClient.obtenerPrecioDeArticulo(item.getArticuloId(), token);
    
            Map<String, Object> it = new HashMap<>();
            it.put("articuloId", item.getArticuloId());
            it.put("cantidad", item.getCantidad());
            it.put("precioUnitario", precio);
    
            itemsPayload.add(it);
        }
    
        Map<String, Object> ordenPayload = new HashMap<>();
        ordenPayload.put("direccionEnvio", nuevaDireccion);
        ordenPayload.put("items", itemsPayload);
    
        // ✅ Orden se envía con token
        orderClient.enviarOrden(ordenPayload, token);
    
        return carrito;
    }
    

    public List<Carrito> obtenerPedidosConfirmados(String email) {
        return carritoRepo.findAll().stream()
                .filter(c -> c.getUsuarioEmail().equals(email) && c.isConfirmado())
                .toList();
    }

    public List<CarritoItem> verItems(String usuarioEmail) {
        Carrito carrito = carritoRepo.findByUsuarioEmailAndConfirmadoFalse(usuarioEmail)
                .orElseThrow(CarritoNotFoundException::new);
        return itemRepo.findByCarrito(carrito);
    }

}
