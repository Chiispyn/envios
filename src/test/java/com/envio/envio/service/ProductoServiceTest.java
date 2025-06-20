package com.envio.envio.service;

import com.envio.envio.model.Producto;
import com.envio.envio.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto1;
    private Producto producto2;

    @BeforeEach
    void setUp() {
        // Productos del DataLoader
        producto1 = new Producto();
        producto1.setIdProducto(1);
        producto1.setNombre("Plato de madera de lenga");
        producto1.setPrecio(3500.00);

        producto2 = new Producto();
        producto2.setIdProducto(2);
        producto2.setNombre("Juego de cubiertos de bambú");
        producto2.setPrecio(4500.00);
    }

    @Test
    void listarProductos_debeRetornarListaDeProductos() {
        // Given
        when(productoRepository.findAll()).thenReturn(Arrays.asList(producto1, producto2));

        // When
        List<Producto> productos = productoService.listarProductos();

        // Then
        assertNotNull(productos);
        assertEquals(2, productos.size());
        assertTrue(productos.contains(producto1));
        assertTrue(productos.contains(producto2));
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    void obtenerProductoPorId_debeRetornarProductoCuandoExiste() {
        // Given
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto1));

        // When
        Optional<Producto> foundProducto = productoService.obtenerProductoPorId(1);

        // Then
        assertTrue(foundProducto.isPresent());
        assertEquals(producto1, foundProducto.get());
        verify(productoRepository, times(1)).findById(1);
    }

    @Test
    void obtenerProductoPorId_debeRetornarVacioCuandoNoExiste() {
        // Given
        when(productoRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<Producto> foundProducto = productoService.obtenerProductoPorId(99);

        // Then
        assertFalse(foundProducto.isPresent());
        verify(productoRepository, times(1)).findById(99);
    }

    @Test
    void guardarProducto_debeGuardarProducto() {
        // Given
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre("Shampoo sólido biodegradable"); // Un producto que podría ser nuevo
        nuevoProducto.setPrecio(5000.0);

        // Cuando el repositorio guarda, devuelve un producto con un ID asignado (simulamos ID 3)
        Producto savedProductoMock = new Producto();
        savedProductoMock.setIdProducto(3);
        savedProductoMock.setNombre("Shampoo sólido biodegradable");
        savedProductoMock.setPrecio(5000.0);

        when(productoRepository.save(any(Producto.class))).thenReturn(savedProductoMock);

        // When
        Producto result = productoService.guardarProducto(nuevoProducto);

        // Then
        assertNotNull(result);
        assertEquals(savedProductoMock.getIdProducto(), result.getIdProducto());
        assertEquals(savedProductoMock.getNombre(), result.getNombre());
        verify(productoRepository, times(1)).save(nuevoProducto);
    }

    @Test
    void actualizarProducto_debeActualizarProductoCuandoExiste() {
        // Given
        Producto updatedInfo = new Producto();
        updatedInfo.setNombre("Plato de madera de roble"); // Nuevo nombre para el producto1
        updatedInfo.setPrecio(4000.0);

        // Simula el producto existente después de la actualización
        Producto existingProductUpdated = new Producto();
        existingProductUpdated.setIdProducto(producto1.getIdProducto());
        existingProductUpdated.setNombre(updatedInfo.getNombre());
        existingProductUpdated.setPrecio(updatedInfo.getPrecio());

        when(productoRepository.findById(producto1.getIdProducto())).thenReturn(Optional.of(producto1));
        when(productoRepository.save(any(Producto.class))).thenReturn(existingProductUpdated);

        // When
        Producto result = productoService.actualizarProducto(producto1.getIdProducto(), updatedInfo);

        // Then
        assertNotNull(result);
        assertEquals("Plato de madera de roble", result.getNombre());
        assertEquals(4000.0, result.getPrecio());
        verify(productoRepository, times(1)).findById(producto1.getIdProducto());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void actualizarProducto_debeRetornarNuloCuandoNoExiste() {
        // Given
        Producto updatedInfo = new Producto();
        updatedInfo.setNombre("Producto Inexistente");

        when(productoRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Producto result = productoService.actualizarProducto(99, updatedInfo);

        // Then
        assertNull(result);
        verify(productoRepository, times(1)).findById(99);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void eliminarProducto_debeEliminarProducto() {
        // Given
        doNothing().when(productoRepository).deleteById(1);

        // When
        productoService.eliminarProducto(1);

        // Then
        verify(productoRepository, times(1)).deleteById(1);
    }
}