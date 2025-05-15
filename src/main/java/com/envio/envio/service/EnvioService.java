package com.envio.envio.service;

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

    public List<Envio> listarEnvios() {
        return envioRepository.findAll();
    }

    public Optional<Envio> obtenerEnvioPorId(Integer id) {
        return envioRepository.findById(id);
    }

    public Envio guardarEnvio(Envio envio) {
        return envioRepository.save(envio);
    }

    public Envio actualizarEnvio(Integer id, Envio envio) {
        return envioRepository.findById(id)
                .map(existingEnvio -> {
                    if (envio.getFechaEnvio() != null) existingEnvio.setFechaEnvio(envio.getFechaEnvio());
                    if (envio.getEstadoPedido() != null) existingEnvio.setEstadoPedido(envio.getEstadoPedido());
                    if (envio.getIdCliente() != null) existingEnvio.setIdCliente(envio.getIdCliente());
                    if (envio.getProductos() != null) existingEnvio.setProductos(envio.getProductos());
                    return envioRepository.save(existingEnvio);
                })
                .orElse(null);
    }

    public void eliminarEnvio(Integer id) {
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
                    envio.setEstadoPedido(nuevoEstado);
                    return envioRepository.save(envio);
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