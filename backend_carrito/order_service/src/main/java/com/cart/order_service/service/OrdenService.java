package com.cart.order_service.service;

import com.cart.order_service.exception.OrdenNotFoundException;
import com.cart.order_service.model.Orden;
import com.cart.order_service.model.OrdenItem;
import com.cart.order_service.repository.OrdenRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrdenService {

    private final OrdenRepository ordenRepository;

    public OrdenService(OrdenRepository ordenRepository) {
        this.ordenRepository = ordenRepository;
    }

    public Orden crearOrden(Orden orden) {
        orden.setNumeroOrden("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // Setea la referencia de cada item a la orden
        for (OrdenItem item : orden.getItems()) {
            item.setOrden(orden);
        }

        return ordenRepository.save(orden);
    }

    public List<Orden> obtenerOrdenesPorUsuario(String email) {
        return ordenRepository.findByUsuarioEmail(email);
    }

    public Orden obtenerOrdenPorNumero(String numeroOrden) {
        Orden orden = ordenRepository.findByNumeroOrden(numeroOrden);
        if (orden == null) {
            throw new OrdenNotFoundException(numeroOrden);
        }
        return orden;
    }
}
