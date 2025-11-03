package org.example.repository;

import org.example.entities.Producto;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ProductoRepository extends ReactiveCrudRepository<Producto, Long> {
    @org.springframework.data.r2dbc.repository.Query("SELECT * FROM productos_bajo_stock(:minimo)")
    Flux<Producto> obtenerProductosBajoStock(Integer minimo);
}
