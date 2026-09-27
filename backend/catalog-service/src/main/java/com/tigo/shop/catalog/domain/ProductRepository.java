package com.tigo.shop.catalog.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Búsqueda por texto (nombre o descripción) y categoría opcionales.
     * El patrón se pasa ya construido y escapado desde el servicio; los parámetros
     * se enlazan como bind variables, por lo que no hay riesgo de SQL injection.
     */
    @Query("""
            select p from Product p
            where (:pattern is null
                   or lower(p.name) like :pattern escape '!'
                   or lower(p.description) like :pattern escape '!')
              and (:category is null or p.category = :category)
            """)
    Page<Product> search(String pattern, String category, Pageable pageable);

    @Query("select distinct p.category from Product p order by p.category")
    List<String> findCategories();

    /**
     * Descuenta stock de forma atómica: el UPDATE solo afecta la fila si hay existencias
     * suficientes, así dos compras concurrentes nunca dejan el stock en negativo.
     * @return 1 si se descontó, 0 si no hay stock suficiente (o el producto no existe).
     */
    @Modifying
    @Query("update Product p set p.stock = p.stock - :quantity where p.id = :id and p.stock >= :quantity")
    int decrementStock(Long id, int quantity);

    @Modifying
    @Query("update Product p set p.stock = p.stock + :quantity where p.id = :id")
    int incrementStock(Long id, int quantity);
}
