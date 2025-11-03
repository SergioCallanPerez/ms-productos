package org.example.controller;

import org.example.dto.ProductoDTO;
import org.example.entities.Producto;
import org.example.exception.APIException;
import org.example.service.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Producto> getAll() {
        return service.findAll();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Producto> getById(@PathVariable Long id) {
        return service.findById(id)
                .switchIfEmpty(Mono.error(new APIException("Producto no encontrado con id: " + id)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Producto> create(@RequestBody Producto producto) {
        return service.save(producto);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Producto> update(@PathVariable Long id, @RequestBody Producto producto) {
        return service.update(id, producto)
                .switchIfEmpty(Mono.error(new APIException("Producto no encontrado con id: " + id)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return service.delete(id);
    }

    // Actualizar Stock
    @PatchMapping(value = "/{id}/stock", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Void> actualizarStock(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer cantidad = body.get("cantidad");
        if (cantidad == null || cantidad <= 0) {
            return Mono.error(new APIException("Cantidad inválida para actualizar stock"));
        }
        return service.actualizarStock(id, cantidad);
    }

    // Obtener productos con bajo stock
    @GetMapping(value = "/bajo-stock/{minimo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ProductoDTO> obtenerProductosBajoStock(@PathVariable Integer minimo) {
        return service.obtenerProductosBajoStock(minimo);
    }
}
