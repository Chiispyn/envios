package com.envio.envio.controller;

import com.envio.envio.assemblers.ProductoModelAssembler; // Importar el assembler
import com.envio.envio.model.Producto; // La entidad Producto
import com.envio.envio.service.ProductoService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoModelAssembler assembler; // Inyecta el assembler

    public ProductoController(ProductoService productoService, ProductoModelAssembler assembler) {
        this.productoService = productoService;
        this.assembler = assembler;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Producto>>> obtenerTodosLosProductos() {
        List<EntityModel<Producto>> productos = productoService.listarProductos().stream()
                                                        .map(assembler::toModel) // Convertir Entidad a EntityModel
                                                        .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(productos,
                linkTo(methodOn(ProductoController.class).obtenerTodosLosProductos()).withSelfRel(),
                linkTo(methodOn(ProductoController.class).crearProducto(null)).withRel("crear-producto")
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> obtenerProducto(@PathVariable Integer id) {
        return productoService.obtenerProductoPorId(id)
                .map(assembler::toModel) // Convertir Entidad a EntityModel
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EntityModel<Producto>> crearProducto(@RequestBody Producto newProducto) {
        // Recibimos la entidad directamente, sin DTOs
        Producto savedProducto = productoService.guardarProducto(newProducto);

        // Convertir la entidad guardada a EntityModel para la respuesta, incluyendo enlaces
        EntityModel<Producto> entityModel = assembler.toModel(savedProducto);
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> actualizarProducto(@PathVariable Integer id, @RequestBody Producto updatedProducto) {
        // Recibimos la entidad directamente
        // Aseguramos que el ID de la ruta se use para la actualización
        updatedProducto.setIdProducto(id);

        Producto result = productoService.actualizarProducto(id, updatedProducto);

        if (result != null) {
            EntityModel<Producto> entityModel = assembler.toModel(result);
            return ResponseEntity.ok(entityModel);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Integer id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}