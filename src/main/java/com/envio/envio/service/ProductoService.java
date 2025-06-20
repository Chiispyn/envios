package com.envio.envio.service;

import com.envio.envio.model.Producto;
import com.envio.envio.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    // Método para listar todos los productos
    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    // Método para obtener un producto por su ID
    public Optional<Producto> obtenerProductoPorId(Integer id) {
        return productoRepository.findById(id);
    }

    // Método para guardar un nuevo producto
    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    // Método para actualizar un producto existente
    public Producto actualizarProducto(Integer id, Producto productoDetalles) {
        return productoRepository.findById(id)
                .map(productoExistente -> {
                    productoExistente.setNombre(productoDetalles.getNombre());
                    productoExistente.setPrecio(productoDetalles.getPrecio());
                    // Otros atributos del producto si los tuvieras
                    return productoRepository.save(productoExistente);
                })
                .orElse(null); // O podrías lanzar una excepción si el producto no existe
    }

    // Método para eliminar un producto
    public void eliminarProducto(Integer id) {
        productoRepository.deleteById(id);
    }

    // Métodos adicionales si se necesitan búsquedas específicas
    public List<Producto> buscarProductosPorNombre(String nombre) {
        // Implementar lógica de búsqueda por nombre si el ProductoRepository lo soporta
        // Ejemplo: return productoRepository.findByNombreContainingIgnoreCase(nombre);
        return productoRepository.findAll(); // Placeholder
    }
}