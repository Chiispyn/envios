package com.envio.envio.controller;

import com.envio.envio.assemblers.ProductoModelAssembler;
import com.envio.envio.model.Producto;
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
    private final ProductoModelAssembler assembler;

    public ProductoController(ProductoService productoService, ProductoModelAssembler assembler) {
        this.productoService = productoService;
        this.assembler = assembler;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Producto>>> obtenerTodosLosProductos() {
        List<EntityModel<Producto>> productos = productoService.listarProductos().stream()
                                             .map(assembler::toModel)
                                             .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(productos,
                linkTo(methodOn(ProductoController.class).obtenerTodosLosProductos()).withSelfRel(),
                linkTo(methodOn(ProductoController.class).crearProducto(null)).withRel("crear-producto")
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> obtenerProducto(@PathVariable Integer id) {
        return productoService.obtenerProductoPorId(id)
                .map(assembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EntityModel<Producto>> crearProducto(@RequestBody Producto newProducto) {
        Producto savedProducto = productoService.guardarProducto(newProducto);
        EntityModel<Producto> entityModel = assembler.toModel(savedProducto);
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> actualizarProducto(@PathVariable Integer id, @RequestBody Producto updatedProducto) {
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
        boolean eliminado = productoService.eliminarProducto(id); 
        if (!eliminado) {
            return ResponseEntity.notFound().build(); 
        }
        return ResponseEntity.noContent().build(); 
}
}