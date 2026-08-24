package com.cart.order_service.service;

import com.cart.order_service.exception.OrdenNotFoundException;
import com.cart.order_service.model.Orden;
import com.cart.order_service.model.OrdenItem;
import com.cart.order_service.repository.OrdenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenServiceTest {

    @Mock
    private OrdenRepository ordenRepository;

    private OrdenService ordenService;

    @BeforeEach
    void setUp() {
        ordenService = new OrdenService(ordenRepository);
    }

    @Test
    void crearOrden_generaNumeroDeOrden_yVinculaCadaItemALaOrden() {
        Orden orden = new Orden();
        OrdenItem item1 = new OrdenItem();
        OrdenItem item2 = new OrdenItem();
        orden.setItems(List.of(item1, item2));
        when(ordenRepository.save(any(Orden.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Orden resultado = ordenService.crearOrden(orden);

        assertThat(resultado.getNumeroOrden()).startsWith("ORD-");
        assertThat(item1.getOrden()).isSameAs(orden);
        assertThat(item2.getOrden()).isSameAs(orden);
    }

    @Test
    void obtenerOrdenPorNumero_lanzaExcepcion_cuandoNoExiste() {
        when(ordenRepository.findByNumeroOrden("ORD-NOEXISTE")).thenReturn(null);

        assertThatThrownBy(() -> ordenService.obtenerOrdenPorNumero("ORD-NOEXISTE"))
                .isInstanceOf(OrdenNotFoundException.class);
    }

    @Test
    void obtenerOrdenPorNumero_retornaOrden_cuandoExiste() {
        Orden orden = new Orden();
        orden.setNumeroOrden("ORD-ABC123");
        when(ordenRepository.findByNumeroOrden("ORD-ABC123")).thenReturn(orden);

        Orden resultado = ordenService.obtenerOrdenPorNumero("ORD-ABC123");

        assertThat(resultado).isSameAs(orden);
    }

    @Test
    void obtenerOrdenesPorUsuario_delegaEnElRepositorio() {
        Orden orden = new Orden();
        when(ordenRepository.findByUsuarioEmail("user@example.com")).thenReturn(List.of(orden));

        List<Orden> resultado = ordenService.obtenerOrdenesPorUsuario("user@example.com");

        assertThat(resultado).containsExactly(orden);
    }
}
