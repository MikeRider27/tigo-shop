package com.cart.catalog_service.controller;

import com.cart.catalog_service.exception.ArticuloNotFoundException;
import com.cart.catalog_service.model.Articulo;
import com.cart.catalog_service.repository.ArticuloRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticuloControllerTest {

    @Mock
    private ArticuloRepository articuloRepository;

    private ArticuloController controller;

    @BeforeEach
    void setUp() {
        controller = new ArticuloController(articuloRepository);
    }

    @Test
    void listarTodos_retornaTodosLosArticulos() {
        Articulo camiseta = Articulo.builder().id(1L).nombre("Camiseta").precio(25.5).stock(10).build();
        when(articuloRepository.findAll()).thenReturn(List.of(camiseta));

        List<Articulo> resultado = controller.listarTodos();

        assertThat(resultado).containsExactly(camiseta);
    }

    @Test
    void obtenerPorId_retornaArticulo_cuandoExiste() {
        Articulo camiseta = Articulo.builder().id(1L).nombre("Camiseta").precio(25.5).stock(10).build();
        when(articuloRepository.findById(1L)).thenReturn(Optional.of(camiseta));

        Articulo resultado = controller.obtenerPorId(1L);

        assertThat(resultado).isEqualTo(camiseta);
    }

    @Test
    void obtenerPorId_lanzaExcepcion_cuandoNoExiste() {
        when(articuloRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.obtenerPorId(99L))
                .isInstanceOf(ArticuloNotFoundException.class);
    }
}
