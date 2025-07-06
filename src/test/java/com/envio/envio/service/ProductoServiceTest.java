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
import java.util.Collections;
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
    private Producto producto3; 

    @BeforeEach
    void setUp() {
        producto1 = new Producto();
        producto1.setIdProducto(1);
        producto1.setNombre("Plato de madera de lenga");
        producto1.setPrecio(3500.00);

        producto2 = new Producto();
        producto2.setIdProducto(2);
        producto2.setNombre("Juego de cubiertos de bambú");
        producto2.setPrecio(4500.00);

        producto3 = new Producto();
        producto3.setIdProducto(3);
        producto3.setNombre("Tenedor de madera de acacia");
        producto3.setPrecio(2000.00);
    }

    @Test
    void listarProductos_debeRetornarListaDeProductos() {
        when(productoRepository.findAll()).thenReturn(Arrays.asList(producto1, producto2));

        List<Producto> productos = productoService.listarProductos();

        assertNotNull(productos);
        assertEquals(2, productos.size());
        assertTrue(productos.contains(producto1));
        assertTrue(productos.contains(producto2));
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    void obtenerProductoPorId_debeRetornarProductoCuandoExiste() {
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto1));

        Optional<Producto> foundProducto = productoService.obtenerProductoPorId(1);

        assertTrue(foundProducto.isPresent());
        assertEquals(producto1, foundProducto.get());
        verify(productoRepository, times(1)).findById(1);
    }

    @Test
    void obtenerProductoPorId_debeRetornarVacioCuandoNoExiste() {
        when(productoRepository.findById(99)).thenReturn(Optional.empty());
        Optional<Producto> foundProducto = productoService.obtenerProductoPorId(99);

        assertFalse(foundProducto.isPresent());
        verify(productoRepository, times(1)).findById(99);
    }

    @Test
    void guardarProducto_debeGuardarProducto() {
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre("Shampoo sólido biodegradable");
        nuevoProducto.setPrecio(5000.0);

        Producto savedProductoMock = new Producto();
        savedProductoMock.setIdProducto(3);
        savedProductoMock.setNombre("Shampoo sólido biodegradable");
        savedProductoMock.setPrecio(5000.0);

        when(productoRepository.save(any(Producto.class))).thenReturn(savedProductoMock);
        Producto result = productoService.guardarProducto(nuevoProducto);
        assertNotNull(result);
        assertEquals(savedProductoMock.getIdProducto(), result.getIdProducto());
        assertEquals(savedProductoMock.getNombre(), result.getNombre());
        verify(productoRepository, times(1)).save(nuevoProducto);
    }

    @Test
    void actualizarProducto_debeActualizarProductoCuandoExiste() {
        Producto updatedInfo = new Producto();
        updatedInfo.setNombre("Plato de madera de roble");
        updatedInfo.setPrecio(4000.0);

        Producto existingProductUpdated = new Producto();
        existingProductUpdated.setIdProducto(producto1.getIdProducto());
        existingProductUpdated.setNombre(updatedInfo.getNombre());
        existingProductUpdated.setPrecio(updatedInfo.getPrecio());

        when(productoRepository.findById(producto1.getIdProducto())).thenReturn(Optional.of(producto1));
        when(productoRepository.save(any(Producto.class))).thenReturn(existingProductUpdated);
        Producto result = productoService.actualizarProducto(producto1.getIdProducto(), updatedInfo);
        assertNotNull(result);
        assertEquals("Plato de madera de roble", result.getNombre());
        assertEquals(4000.0, result.getPrecio());
        verify(productoRepository, times(1)).findById(producto1.getIdProducto());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void actualizarProducto_debeRetornarNuloCuandoNoExiste() {
        Producto updatedInfo = new Producto();
        updatedInfo.setNombre("Producto Inexistente");

        when(productoRepository.findById(99)).thenReturn(Optional.empty());
        Producto result = productoService.actualizarProducto(99, updatedInfo);
        assertNull(result);
        verify(productoRepository, times(1)).findById(99);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void eliminarProducto_debeEliminarProductoCuandoExiste() {
        when(productoRepository.existsById(1)).thenReturn(true);
        doNothing().when(productoRepository).deleteById(1);
        boolean result = productoService.eliminarProducto(1);
        assertTrue(result);
        verify(productoRepository, times(1)).existsById(1);
        verify(productoRepository, times(1)).deleteById(1);
    }

    @Test
    void eliminarProducto_debeRetornarFalsoCuandoNoExiste() {
        when(productoRepository.existsById(99)).thenReturn(false);
        boolean result = productoService.eliminarProducto(99);
        assertFalse(result);
        verify(productoRepository, times(1)).existsById(99);
        verify(productoRepository, never()).deleteById(anyInt());
    }

    @Test
    void buscarProductosPorNombre_debeRetornarTodosLosProductosIndependienteDelNombre() {
        List<Producto> todosLosProductos = Arrays.asList(producto1, producto2, producto3);
        when(productoRepository.findAll()).thenReturn(todosLosProductos);
        List<Producto> productosEncontrados = productoService.buscarProductosPorNombre("inexistente");
        assertNotNull(productosEncontrados);
        assertEquals(todosLosProductos.size(), productosEncontrados.size());
        assertTrue(productosEncontrados.containsAll(todosLosProductos));
        verify(productoRepository, times(1)).findAll(); 
    }

    @Test
    void buscarProductosPorNombre_debeRetornarTodosLosProductosConNombreExacto() {
        List<Producto> todosLosProductos = Arrays.asList(producto1, producto2, producto3);
        when(productoRepository.findAll()).thenReturn(todosLosProductos);
        List<Producto> productosEncontrados = productoService.buscarProductosPorNombre("Plato de madera de lenga");
        assertNotNull(productosEncontrados);
        assertEquals(todosLosProductos.size(), productosEncontrados.size());
        assertTrue(productosEncontrados.containsAll(todosLosProductos));
        verify(productoRepository, times(1)).findAll(); 
    }

    @Test
    void buscarProductosPorNombre_debeRetornarTodosLosProductosConNombreParcial() {
        List<Producto> todosLosProductos = Arrays.asList(producto1, producto2, producto3);
        when(productoRepository.findAll()).thenReturn(todosLosProductos);
        List<Producto> productosEncontrados = productoService.buscarProductosPorNombre("madera");
        assertNotNull(productosEncontrados);
        assertEquals(todosLosProductos.size(), productosEncontrados.size());
        assertTrue(productosEncontrados.containsAll(todosLosProductos));
        verify(productoRepository, times(1)).findAll(); 
    }

    @Test
    void buscarProductosPorNombre_debeRetornarTodosLosProductosCuandoElRepositorioEstaVacio() {
        when(productoRepository.findAll()).thenReturn(Collections.emptyList());
        List<Producto> productosEncontrados = productoService.buscarProductosPorNombre("cualquierCosa");
        assertNotNull(productosEncontrados);
        assertTrue(productosEncontrados.isEmpty());
        assertEquals(0, productosEncontrados.size());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    void buscarProductosPorNombre_debeRetornarTodosLosProductosSiElNombreEsVacioONulo() {
        List<Producto> todosLosProductos = Arrays.asList(producto1, producto2, producto3);
        when(productoRepository.findAll()).thenReturn(todosLosProductos);
        List<Producto> productosPorNombreVacio = productoService.buscarProductosPorNombre("");
        assertNotNull(productosPorNombreVacio);
        assertEquals(3, productosPorNombreVacio.size());
        assertTrue(productosPorNombreVacio.containsAll(todosLosProductos));
        List<Producto> productosPorNombreNulo = productoService.buscarProductosPorNombre(null);
        assertNotNull(productosPorNombreNulo);
        assertEquals(3, productosPorNombreNulo.size());
        assertTrue(productosPorNombreNulo.containsAll(todosLosProductos));
        verify(productoRepository, times(2)).findAll();
    }
}