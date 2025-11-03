package org.example.service;

import org.example.dto.ProductoDTO;
import org.example.entities.Producto;
import org.example.exception.APIException;
import org.example.exception.R2dbcExceptionUtil;
import org.example.repository.ProductoRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class ProductoService {

    private final ProductoRepository repository;
    private final DatabaseClient databaseClient;

    public ProductoService(ProductoRepository repository, DatabaseClient databaseClient) {
        this.repository = repository;
        this.databaseClient = databaseClient;
    }

    public Flux<Producto> findAll() {
        return repository.findAll();
    }

    public Mono<Producto> findById(Long id) {
        return repository.findById(id);
    }

    public Mono<Producto> save(Producto producto) {
        return repository.save(producto);
    }

    public Mono<Producto> update(Long id, Producto updated) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new APIException("Producto no encontrado con id: " + id)))
                .flatMap(existing -> {
                    existing.setNombre(updated.getNombre());
                    existing.setDescripcion(updated.getDescripcion());
                    existing.setPrecio(updated.getPrecio());
                    existing.setStock(updated.getStock());
                    existing.setActivo(updated.getActivo());
                    return repository.save(existing);
                });
    }

    public Mono<Void> delete(Long id) {
        return repository.deleteById(id);
    }

    // Llamada a actualizar_stock
    public Mono<Void> actualizarStock(Long productoId, Integer cantidad) {
        return databaseClient
                .sql("SELECT actualizar_stock(:productoId, :cantidad)")
                .bind("productoId", productoId)
                .bind("cantidad", cantidad)
                .then()
                .onErrorMap(R2dbcExceptionUtil::handleR2dbcException);
    }

    // Llamada a productos_bajo_stock
    public Flux<ProductoDTO> obtenerProductosBajoStock(Integer minimo) {
        return databaseClient
                .sql("SELECT * FROM productos_bajo_stock(:minimo)")
                .bind("minimo", minimo)
                .map((row, metadata) -> {
                    ProductoDTO p = new ProductoDTO();
                    p.setId(row.get("id", Long.class));
                    p.setNombre(row.get("nombre", String.class));
                    p.setStock(row.get("stock", Integer.class));
                    return p;
                })
                .all()
                .onErrorMap(R2dbcExceptionUtil::handleR2dbcException);
    }
}
