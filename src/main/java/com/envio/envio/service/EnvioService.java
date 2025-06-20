package com.envio.envio.service;

import com.envio.envio.integration.ChilexpressApiService;
import com.envio.envio.model.Envio;
import com.envio.envio.model.Estado;
import com.envio.envio.model.Producto;
import com.envio.envio.repository.EnvioRepository;
import com.envio.envio.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class EnvioService {

    @Autowired
    private EnvioRepository envioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ChilexpressApiService chilexpressApiService;

    public List<Envio> listarEnvios() {
        return envioRepository.findAll();
    }

    public Optional<Envio> obtenerEnvioPorId(Integer id) {
        return envioRepository.findById(id);
    }

    public Envio guardarEnvio(Envio envio) {
        Envio nuevoEnvio = envioRepository.save(envio);
        chilexpressApiService.notificarNuevoEnvio(nuevoEnvio.getIdEnvio());
        return nuevoEnvio;
    }

    public Envio actualizarEnvio(Integer id, Envio envio) {
        return envioRepository.findById(id)
                .map(existingEnvio -> {
                    Estado estadoAnterior = existingEnvio.getEstadoPedido();
                    if (envio.getFechaEnvio() != null) existingEnvio.setFechaEnvio(envio.getFechaEnvio());
                    if (envio.getEstadoPedido() != null) existingEnvio.setEstadoPedido(envio.getEstadoPedido());
                    if (envio.getIdCliente() != null) existingEnvio.setIdCliente(envio.getIdCliente());
                    // Asegúrate de que la colección de productos se maneje correctamente, por ejemplo, fusionando o reemplazando
                    if (envio.getProductos() != null) existingEnvio.setProductos(envio.getProductos());
                    Envio envioActualizado = envioRepository.save(existingEnvio);
                    if (envio.getEstadoPedido() != null && !envio.getEstadoPedido().equals(estadoAnterior)) {
                        chilexpressApiService.actualizarEstadoEnvio(envioActualizado.getIdEnvio(), envioActualizado.getEstadoPedido().toString());
                    }
                    return envioActualizado;
                })
                .orElse(null);
    }

    public void eliminarEnvio(Integer id) {
        Optional<Envio> envioOptional = envioRepository.findById(id);
        envioOptional.ifPresent(envio -> chilexpressApiService.cancelarEnvio(envio.getIdEnvio()));
        envioRepository.deleteById(id);
    }

    public Envio agregarProducto(Integer idEnvio, Integer idProducto) {
        Optional<Envio> envioOptional = envioRepository.findById(idEnvio);
        Optional<Producto> productoOptional = productoRepository.findById(idProducto);

        if (envioOptional.isPresent() && productoOptional.isPresent()) {
            Envio envio = envioOptional.get();
            Producto producto = productoOptional.get();
            envio.getProductos().add(producto);
            return envioRepository.save(envio);
        }
        return null;
    }

    public Envio cambiarEstado(Integer idEnvio, Estado nuevoEstado) {
        return envioRepository.findById(idEnvio)
                .map(envio -> {
                    Estado estadoAnterior = envio.getEstadoPedido();
                    envio.setEstadoPedido(nuevoEstado);
                    Envio envioActualizado = envioRepository.save(envio);
                    if (!nuevoEstado.equals(estadoAnterior)) {
                        chilexpressApiService.actualizarEstadoEnvio(envioActualizado.getIdEnvio(), nuevoEstado.toString());
                    }
                    return envioActualizado;
                })
                .orElse(null);
    }

    public Envio eliminarProducto(Integer idEnvio, Integer idProducto) {
        Optional<Envio> envioOptional = envioRepository.findById(idEnvio);
        Optional<Producto> productoOptional = productoRepository.findById(idProducto);

        if (envioOptional.isPresent() && productoOptional.isPresent()) {
            Envio envio = envioOptional.get();
            Producto producto = productoOptional.get();
            envio.getProductos().remove(producto);
            return envioRepository.save(envio);
        }
        return null;
    }

    public List<Producto> obtenerProductosDelEnvio(Integer idEnvio) {
        Optional<Envio> envioOptional = envioRepository.findById(idEnvio);
        return envioOptional.map(envio -> new ArrayList<>(envio.getProductos()))
                               .orElse(new ArrayList<>());
    }
}