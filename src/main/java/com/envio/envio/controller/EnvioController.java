package com.envio.envio.controller;

import com.envio.envio.model.Envio;
import com.envio.envio.model.Estado;
import com.envio.envio.model.Producto;
import com.envio.envio.service.EnvioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/envios")
public class EnvioController {

    @Autowired
    private EnvioService envioService;

    @GetMapping
    public ResponseEntity<List<Envio>> obtenerTodosLosEnvios() {
        return new ResponseEntity<>(envioService.listarEnvios(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Envio> obtenerEnvio(@PathVariable Integer id) {
        Optional<Envio> envio = envioService.obtenerEnvioPorId(id);
        return envio.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Envio> crearEnvio(@RequestBody Envio envio) {
        Envio nuevoEnvio = envioService.guardarEnvio(envio);
        return new ResponseEntity<>(nuevoEnvio, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Envio> actualizarEnvio(@PathVariable Integer id, @RequestBody Envio envio) {
        Envio envioActualizado = envioService.actualizarEnvio(id, envio);
        if (envioActualizado != null) {
            return new ResponseEntity<>(envioActualizado, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEnvio(@PathVariable Integer id) {
        envioService.eliminarEnvio(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{idEnvio}/productos/{idProducto}")
    public ResponseEntity<Envio> agregarProductoAlEnvio(@PathVariable Integer idEnvio, @PathVariable Integer idProducto) {
        Envio envioActualizado = envioService.agregarProducto(idEnvio, idProducto);
        return envioActualizado != null ? new ResponseEntity<>(envioActualizado, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/{idEnvio}/estado")
    public ResponseEntity<Envio> cambiarEstadoDelEnvio(@PathVariable Integer idEnvio, @RequestBody Estado nuevoEstado) {
        Envio envioActualizado = envioService.cambiarEstado(idEnvio, nuevoEstado);
        return envioActualizado != null ? new ResponseEntity<>(envioActualizado, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{idEnvio}/productos/{idProducto}")
    public ResponseEntity<Envio> eliminarProductoDelEnvio(@PathVariable Integer idEnvio, @PathVariable Integer idProducto) {
        Envio envioActualizado = envioService.eliminarProducto(idEnvio, idProducto);
        return envioActualizado != null ? new ResponseEntity<>(envioActualizado, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{idEnvio}/productos")
    public ResponseEntity<List<Producto>> obtenerProductosDelEnvio(@PathVariable Integer idEnvio) {
        List<Producto> productos = envioService.obtenerProductosDelEnvio(idEnvio);
        return new ResponseEntity<>(productos, HttpStatus.OK);
    }
}