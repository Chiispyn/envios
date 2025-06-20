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
@RequestMapping("/api/envios")
public class EnvioController {

    private final EnvioService envioService;
    private final EnvioModelAssembler assembler; 

    public EnvioController(EnvioService envioService, EnvioModelAssembler assembler) {
        this.envioService = envioService;
        this.assembler = assembler;
    }

    /**
     * Obtiene todos los envíos con enlaces HATEOAS.
     * @return Una respuesta HTTP con la lista de envíos.
     */
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

    /**
     * Obtiene un envío específico por su ID con enlaces HATEOAS.
     * @param id El ID del envío.
     * @return Una respuesta HTTP con el envío encontrado o un 404 si no existe.
     */
    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<EntityModel<Envio>> obtenerEnvio(@PathVariable Integer id) {
        return envioService.obtenerEnvioPorId(id)
                .map(assembler::toModel) // Convertimos la entidad Envio a EntityModel
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo envío y lo retorna con enlaces HATEOAS.
     * @param newEnvio El objeto Envio a crear.
     * @return Una respuesta HTTP 201 Created con el envío creado.
     */
    @PostMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<EntityModel<Envio>> crearEnvio(@RequestBody Envio newEnvio) {
        Envio savedEnvio = envioService.guardarEnvio(newEnvio);
        EntityModel<Envio> entityModel = assembler.toModel(savedEnvio); // Convertimos la entidad guardada a EntityModel

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    /**
     * Actualiza un envío existente por su ID y lo retorna con enlaces HATEOAS.
     * @param id El ID del envío a actualizar.
     * @param updatedEnvio El objeto Envio con la información actualizada.
     * @return Una respuesta HTTP 200 OK con el envío actualizado o un 404 si no existe.
     */
    @PutMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<EntityModel<Envio>> actualizarEnvio(@PathVariable Integer id, @RequestBody Envio updatedEnvio) {
        // Aseguramos que el ID de la ruta se use para la actualización
        updatedEnvio.setIdEnvio(id);
        Envio result = envioService.actualizarEnvio(id, updatedEnvio);

        if (result != null) {
            EntityModel<Envio> entityModel = assembler.toModel(result); // Convertimos la entidad actualizada a EntityModel
            return ResponseEntity.ok(entityModel);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Elimina un envío por su ID.
     * @param id El ID del envío a eliminar.
     * @return Una respuesta HTTP 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarEnvio(@PathVariable Integer id) {
        envioService.eliminarEnvio(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cambia el estado de un envío por su ID y lo retorna con enlaces HATEOAS.
     * @param idEnvio El ID del envío.
     * @param nuevoEstado El nuevo estado (PENDIENTE, EN_CAMINO, ENTREGADO, CANCELADO).
     * @return Una respuesta HTTP 200 OK con el envío actualizado.
     */
    @PutMapping(value = "/{idEnvio}/estado", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<EntityModel<Envio>> cambiarEstadoDelEnvio(@PathVariable Integer idEnvio, @RequestBody Estado nuevoEstado) {
        Envio updatedEnvio = envioService.cambiarEstado(idEnvio, nuevoEstado);
        if (updatedEnvio != null) {
            return ResponseEntity.ok(assembler.toModel(updatedEnvio)); // Convertimos la entidad a EntityModel
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Agrega un producto a un envío existente y retorna el envío actualizado con HATEOAS.
     * @param idEnvio El ID del envío.
     * @param idProducto El ID del producto a agregar.
     * @return Una respuesta HTTP 200 OK con el envío actualizado.
     */
    @PostMapping(value = "/{idEnvio}/productos/{idProducto}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<EntityModel<Envio>> agregarProductoAlEnvio(@PathVariable Integer idEnvio, @PathVariable Integer idProducto) {
        Envio updatedEnvio = envioService.agregarProducto(idEnvio, idProducto);
        if (updatedEnvio != null) {
            return ResponseEntity.ok(assembler.toModel(updatedEnvio)); // Convertimos la entidad a EntityModel
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Elimina un producto de un envío existente y retorna el envío actualizado con HATEOAS.
     * @param idEnvio El ID del envío.
     * @param idProducto El ID del producto a eliminar.
     * @return Una respuesta HTTP 200 OK con el envío actualizado.
     */
    @DeleteMapping(value = "/{idEnvio}/productos/{idProducto}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<EntityModel<Envio>> eliminarProductoDelEnvio(@PathVariable Integer idEnvio, @PathVariable Integer idProducto) {
        Envio updatedEnvio = envioService.eliminarProducto(idEnvio, idProducto);
        if (updatedEnvio != null) {
            return ResponseEntity.ok(assembler.toModel(updatedEnvio)); // Convertimos la entidad a EntityModel
        }
        return ResponseEntity.ok().build(); // Podría ser 204 No Content si no se devuelve cuerpo.
                                           // Si quieres devolver el estado actual, 200 OK es correcto.
    }

    /**
     * Obtiene la lista de productos de un envío específico.
     * Nota: Esta operación retorna solo los productos, no el envoltorio completo del envío.
     * Si deseas HATEOAS para los productos aquí, necesitarías inyectar `ProductoModelAssembler`
     * y convertir cada `Producto` a `EntityModel<Producto>`.
     * @param idEnvio El ID del envío.
     * @return Una respuesta HTTP con la lista de productos.
     */
    @GetMapping(value = "/{idEnvio}/productos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Producto>> obtenerProductosDelEnvio(@PathVariable Integer idEnvio) {
        List<Producto> productos = envioService.obtenerProductosDelEnvio(idEnvio);
        if (productos != null) {
            return ResponseEntity.ok(productos);
        }
        return ResponseEntity.notFound().build();
    }
}