package com.cart.catalog_service.service;

import com.cart.catalog_service.model.Articulo;
import com.cart.catalog_service.repository.ArticuloRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArticuloService {

    private final ArticuloRepository articuloRepository;

    public ArticuloService(ArticuloRepository articuloRepository) {
        this.articuloRepository = articuloRepository;
    }

    public List<Articulo> listarTodos() {
        return articuloRepository.findAll();
    }

    public Optional<Articulo> obtenerPorId(Long id) {
        return articuloRepository.findById(id);
    }

    public List<Articulo> buscarPorNombre(String nombre) {
        return articuloRepository.findByNombreContainingIgnoreCase(nombre);
    }
}
