package com.cart.catalog_service.controller;

import com.cart.catalog_service.exception.ArticuloNotFoundException;
import com.cart.catalog_service.model.Articulo;
import com.cart.catalog_service.repository.ArticuloRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articulos")
public class ArticuloController {

    private final ArticuloRepository articuloRepository;

    public ArticuloController(ArticuloRepository articuloRepository) {
        this.articuloRepository = articuloRepository;
    }

    @GetMapping
    public List<Articulo> listarTodos() {
        return articuloRepository.findAll();
    }

    @GetMapping("/{id}")
    public Articulo obtenerPorId(@PathVariable Long id) {
        return articuloRepository.findById(id)
                .orElseThrow(() -> new ArticuloNotFoundException(id));
    }

}
