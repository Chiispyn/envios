package com.envio.envio.controller;

import com.envio.envio.assemblers.EnvioModelAssembler;
import com.envio.envio.model.Envio;
import com.envio.envio.model.Estado;
import com.envio.envio.model.Producto;
import com.envio.envio.service.EnvioService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/envios")
public class EnvioController {

    private final EnvioService envioService;
    private final EnvioModelAssembler assembler; 

    public EnvioController(EnvioService envioService, EnvioModelAssembler assembler) {
        this.envioService = envioService;
        this.assembler = assembler;
    }

   
    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<CollectionModel<EntityModel<Envio>>> obtenerTodosLosEnvios() {
        List<EntityModel<Envio>> envios = envioService.listarEnvios().stream()
                .map(assembler::toModel) // Convertimos cada entidad Envio a EntityModel
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                CollectionModel.of(envios,
                        linkTo(methodOn(EnvioController.class).obtenerTodosLosEnvios()).withSelfRel(),
                        linkTo(methodOn(EnvioController.class).crearEnvio(null)).withRel("crear-envio")
                )
        );
    }

 
    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<EntityModel<Envio>> obtenerEnvio(@PathVariable Integer id) {
        return envioService.obtenerEnvioPorId(id)
                .map(assembler::toModel) 
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

 
    @PostMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<EntityModel<Envio>> crearEnvio(@RequestBody Envio newEnvio) {
        Envio savedEnvio = envioService.guardarEnvio(newEnvio);
        EntityModel<Envio> entityModel = assembler.toModel(savedEnvio); 

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

   
    @PutMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<EntityModel<Envio>> actualizarEnvio(@PathVariable Integer id, @RequestBody Envio updatedEnvio) {
        updatedEnvio.setIdEnvio(id);
        Envio result = envioService.actualizarEnvio(id, updatedEnvio);

        if (result != null) {
            EntityModel<Envio> entityModel = assembler.toModel(result); 
            return ResponseEntity.ok(entityModel);
        }
        return ResponseEntity.notFound().build();
    }

  
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarEnvio(@PathVariable Integer id) {
        envioService.eliminarEnvio(id);
        return ResponseEntity.noContent().build();
    }

    
    @PutMapping(value = "/{idEnvio}/estado", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<EntityModel<Envio>> cambiarEstadoDelEnvio(@PathVariable Integer idEnvio, @RequestBody Estado nuevoEstado) {
        Envio updatedEnvio = envioService.cambiarEstado(idEnvio, nuevoEstado);
        if (updatedEnvio != null) {
            return ResponseEntity.ok(assembler.toModel(updatedEnvio)); }
        return ResponseEntity.notFound().build();
    }

   
    @PostMapping(value = "/{idEnvio}/productos/{idProducto}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<EntityModel<Envio>> agregarProductoAlEnvio(@PathVariable Integer idEnvio, @PathVariable Integer idProducto) {
        Envio updatedEnvio = envioService.agregarProducto(idEnvio, idProducto);
        if (updatedEnvio != null) {
            return ResponseEntity.ok(assembler.toModel(updatedEnvio)); 
        }
        return ResponseEntity.notFound().build();
    }

    
    @DeleteMapping(value = "/{idEnvio}/productos/{idProducto}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<EntityModel<Envio>> eliminarProductoDelEnvio(@PathVariable Integer idEnvio, @PathVariable Integer idProducto) {
    Envio updatedEnvio = envioService.eliminarProducto(idEnvio, idProducto);
    if (updatedEnvio != null) {
        return ResponseEntity.ok(assembler.toModel(updatedEnvio));
    }
    return ResponseEntity.notFound().build(); 
}


    @GetMapping(value = "/{idEnvio}/productos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Producto>> obtenerProductosDelEnvio(@PathVariable Integer idEnvio) {
        List<Producto> productos = envioService.obtenerProductosDelEnvio(idEnvio);
        if (productos != null) {
            return ResponseEntity.ok(productos);
        }
        return ResponseEntity.notFound().build();
    }
}
