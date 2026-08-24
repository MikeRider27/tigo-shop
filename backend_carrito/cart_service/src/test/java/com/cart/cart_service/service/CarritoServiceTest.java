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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepo;

    @Mock
    private CarritoItemRepository itemRepo;

    @Mock
    private OrderClient orderClient;

    @Mock
    private CatalogClient catalogClient;

    private CarritoService carritoService;

    @BeforeEach
    void setUp() {
        carritoService = new CarritoService(carritoRepo, itemRepo, orderClient, catalogClient);
    }

    @Test
    void agregarItem_creaNuevoItem_cuandoArticuloNoEstaEnElCarrito() {
        Carrito carrito = carritoConId(1L);
        when(carritoRepo.findByUsuarioEmailAndConfirmadoFalse("user@example.com"))
                .thenReturn(Optional.of(carrito));
        when(itemRepo.findByCarritoAndArticuloId(carrito, 10L)).thenReturn(null);
        when(carritoRepo.findById(1L)).thenReturn(Optional.of(carrito));

        carritoService.agregarItem("user@example.com", 10L, 2);

        verify(itemRepo).save(argThat(item -> item.getArticuloId().equals(10L) && item.getCantidad() == 2));
    }

    @Test
    void agregarItem_incrementaCantidad_cuandoArticuloYaEstaEnElCarrito() {
        Carrito carrito = carritoConId(1L);
        CarritoItem existente = new CarritoItem();
        existente.setArticuloId(10L);
        existente.setCantidad(3);

        when(carritoRepo.findByUsuarioEmailAndConfirmadoFalse("user@example.com"))
                .thenReturn(Optional.of(carrito));
        when(itemRepo.findByCarritoAndArticuloId(carrito, 10L)).thenReturn(existente);
        when(carritoRepo.findById(1L)).thenReturn(Optional.of(carrito));

        carritoService.agregarItem("user@example.com", 10L, 2);

        assertThat(existente.getCantidad()).isEqualTo(5);
        verify(itemRepo).save(existente);
    }

    @Test
    void eliminarItemDelUsuario_lanzaExcepcion_siItemNoExiste() {
        Carrito carrito = carritoConId(1L);
        when(carritoRepo.findByUsuarioEmailAndConfirmadoFalse("user@example.com"))
                .thenReturn(Optional.of(carrito));
        when(itemRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carritoService.eliminarItemDelUsuario("user@example.com", 99L))
                .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void eliminarItemDelUsuario_lanzaExcepcion_siItemPerteneceAOtroCarrito() {
        Carrito carritoDelUsuario = carritoConId(1L);
        Carrito carritoDeOtro = carritoConId(2L);
        CarritoItem item = new CarritoItem();
        item.setCarrito(carritoDeOtro);

        when(carritoRepo.findByUsuarioEmailAndConfirmadoFalse("user@example.com"))
                .thenReturn(Optional.of(carritoDelUsuario));
        when(itemRepo.findById(5L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> carritoService.eliminarItemDelUsuario("user@example.com", 5L))
                .isInstanceOf(ItemNotOwnedException.class);

        verify(itemRepo, never()).delete(any());
    }

    @Test
    void eliminarItemDelUsuario_eliminaItem_cuandoPerteneceAlUsuario() {
        Carrito carrito = carritoConId(1L);
        CarritoItem item = new CarritoItem();
        item.setCarrito(carrito);

        when(carritoRepo.findByUsuarioEmailAndConfirmadoFalse("user@example.com"))
                .thenReturn(Optional.of(carrito));
        when(itemRepo.findById(5L)).thenReturn(Optional.of(item));

        carritoService.eliminarItemDelUsuario("user@example.com", 5L);

        verify(itemRepo).delete(item);
    }

    @Test
    void verItems_lanzaExcepcion_siNoHayCarritoActivo() {
        when(carritoRepo.findByUsuarioEmailAndConfirmadoFalse("user@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> carritoService.verItems("user@example.com"))
                .isInstanceOf(CarritoNotFoundException.class);
    }

    private Carrito carritoConId(Long id) {
        Carrito carrito = new Carrito();
        carrito.setId(id);
        carrito.setUsuarioEmail("user@example.com");
        carrito.setDireccionEnvio("");
        return carrito;
    }

    private CarritoItem argThat(java.util.function.Predicate<CarritoItem> predicate) {
        return org.mockito.ArgumentMatchers.argThat(predicate::test);
    }
}
