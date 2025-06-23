package com.envio.envio.service;

import com.envio.envio.integration.ChilexpressApiService;
import com.envio.envio.model.Envio;
import com.envio.envio.model.Estado;
import com.envio.envio.model.Producto;
import com.envio.envio.repository.EnvioRepository;
import com.envio.envio.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
// No se necesita @ActiveProfiles("test") aquí porque es un test unitario del servicio
class EnvioServiceTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ChilexpressApiService chilexpressApiService;

    @InjectMocks
    private EnvioService envioService;

    private Envio envio1;
    private Envio envio2;
    private Producto p1;
    private Producto p2;
    private Producto p3;

    @BeforeEach
    void setUp() {
        // Productos
        p1 = new Producto();
        p1.setIdProducto(1);
        p1.setNombre("Plato de madera de lenga");
        p1.setPrecio(3500.00);

        p2 = new Producto();
        p2.setIdProducto(2);
        p2.setNombre("Juego de cubiertos de bambú");
        p2.setPrecio(4500.00);

        p3 = new Producto();
        p3.setIdProducto(3);
        p3.setNombre("Shampoo sólido biodegradable");
        p3.setPrecio(5000.00);

        // Envios
        envio1 = new Envio();
        envio1.setIdEnvio(1L);
        envio1.setEstadoPedido(Estado.PENDIENTE);
        envio1.setIdCliente(101);
        envio1.setFechaEnvio(new Date());
        Set<Producto> productosEnvio1 = new HashSet<>();
        productosEnvio1.add(p1);
        productosEnvio1.add(p2);
        envio1.setProductos(productosEnvio1);

        envio2 = new Envio();
        envio2.setIdEnvio(2L);
        envio2.setEstadoPedido(Estado.EN_CAMINO);
        envio2.setIdCliente(102);
        envio2.setFechaEnvio(new Date(System.currentTimeMillis() + 86400000));
        Set<Producto> productosEnvio2 = new HashSet<>();
        productosEnvio2.add(p3);
        envio2.setProductos(productosEnvio2);
    }

    @Test
    void listarEnvios_debeRetornarListaDeEnvios() {
        // Given
        when(envioRepository.findAll()).thenReturn(Arrays.asList(envio1, envio2));

        // When
        List<Envio> envios = envioService.listarEnvios();

        // Then
        assertNotNull(envios);
        assertEquals(2, envios.size());
        assertEquals(envio1, envios.get(0));
        assertEquals(envio2, envios.get(1));
        verify(envioRepository, times(1)).findAll();
    }

    @Test
    void obtenerEnvioPorId_debeRetornarEnvioCuandoExiste() {
        // Given
        when(envioRepository.findById(1)).thenReturn(Optional.of(envio1));

        // When
        Optional<Envio> foundEnvio = envioService.obtenerEnvioPorId(1);

        // Then
        assertTrue(foundEnvio.isPresent());
        assertEquals(foundEnvio.get().getIdEnvio(),envio1.getIdEnvio());
        verify(envioRepository, times(1)).findById(1);
    }

    @Test
    void obtenerEnvioPorId_debeRetornarVacioCuandoNoExiste() {
        // Given
        when(envioRepository.findById(99)).thenReturn(Optional.empty());

        // When
        Optional<Envio> foundEnvio = envioService.obtenerEnvioPorId(99);

        // Then
        assertFalse(foundEnvio.isPresent());
        verify(envioRepository, times(1)).findById(99);
    }

    @Test
    void guardarEnvio_debeGuardarYNotificarChilexpress() {
        // Given
        Envio nuevoEnvio = new Envio();
        nuevoEnvio.setEstadoPedido(Estado.PENDIENTE);
        nuevoEnvio.setIdCliente(103);
        nuevoEnvio.setFechaEnvio(new Date());
        nuevoEnvio.setProductos(new HashSet<>(Arrays.asList(p1)));

        Envio savedEnvioMock = new Envio();
        savedEnvioMock.setIdEnvio(3L);
        savedEnvioMock.setEstadoPedido(Estado.PENDIENTE);
        savedEnvioMock.setIdCliente(103);
        savedEnvioMock.setFechaEnvio(nuevoEnvio.getFechaEnvio());
        savedEnvioMock.setProductos(nuevoEnvio.getProductos());


        when(envioRepository.save(nuevoEnvio)).thenReturn(savedEnvioMock);
        when(chilexpressApiService.notificarNuevoEnvio(anyLong())).thenReturn(true);

        // When
        Envio result = envioService.guardarEnvio(nuevoEnvio);

        // Then
        assertNotNull(result);
        assertEquals(savedEnvioMock.getIdEnvio(), result.getIdEnvio());
        verify(envioRepository, times(1)).save(nuevoEnvio);
        verify(chilexpressApiService, times(1)).notificarNuevoEnvio(savedEnvioMock.getIdEnvio());
    }

    @Test
    void actualizarEnvio_debeActualizarYNotificarCambioEstado() {
        // Given
        Envio updatedInfo = new Envio();
        updatedInfo.setEstadoPedido(Estado.ENTREGADO);
        updatedInfo.setFechaEnvio(new Date());

        Envio existingEnvioCloned = new Envio();
        existingEnvioCloned.setIdEnvio(envio1.getIdEnvio());
        existingEnvioCloned.setEstadoPedido(envio1.getEstadoPedido());
        existingEnvioCloned.setIdCliente(envio1.getIdCliente());
        existingEnvioCloned.setFechaEnvio(envio1.getFechaEnvio());
        existingEnvioCloned.setProductos(new HashSet<>(envio1.getProductos()));


        when(envioRepository.findById(1)).thenReturn(Optional.of(existingEnvioCloned));
        when(envioRepository.save(any(Envio.class))).thenReturn(existingEnvioCloned);
        when(chilexpressApiService.actualizarEstadoEnvio(anyLong(), anyString())).thenReturn(true);

        // When
        Envio result = envioService.actualizarEnvio(1, updatedInfo);

        // Then
        assertNotNull(result);
        assertEquals(Estado.ENTREGADO, result.getEstadoPedido());
        verify(envioRepository, times(1)).findById(1);
        verify(envioRepository, times(1)).save(existingEnvioCloned);
        verify(chilexpressApiService, times(1)).actualizarEstadoEnvio(existingEnvioCloned.getIdEnvio(), Estado.ENTREGADO.toString());
    }

    @Test
    void actualizarEnvio_debeActualizarSinNotificarCambioEstadoSiEstadoEsMismo() {
        // Given
        Envio updatedInfo = new Envio();
        updatedInfo.setEstadoPedido(Estado.PENDIENTE);
        updatedInfo.setFechaEnvio(new Date());

        Envio existingEnvioCloned = new Envio();
        existingEnvioCloned.setIdEnvio(envio1.getIdEnvio());
        existingEnvioCloned.setEstadoPedido(envio1.getEstadoPedido());
        existingEnvioCloned.setIdCliente(envio1.getIdCliente());
        existingEnvioCloned.setFechaEnvio(envio1.getFechaEnvio());
        existingEnvioCloned.setProductos(new HashSet<>(envio1.getProductos()));

        when(envioRepository.findById(1)).thenReturn(Optional.of(existingEnvioCloned));
        when(envioRepository.save(any(Envio.class))).thenReturn(existingEnvioCloned);

        // When
        Envio result = envioService.actualizarEnvio(1, updatedInfo);

        // Then
        assertNotNull(result);
        assertEquals(Estado.PENDIENTE, result.getEstadoPedido());
        verify(envioRepository, times(1)).findById(1);
        verify(envioRepository, times(1)).save(existingEnvioCloned);
        verify(chilexpressApiService, never()).actualizarEstadoEnvio(anyLong(), anyString());
    }

    @Test
    void eliminarEnvio_debeEliminarYCancelarEnChilexpress() {
        // Given
        when(envioRepository.findById(1)).thenReturn(Optional.of(envio1));
        doNothing().when(envioRepository).deleteById(1);
        when(chilexpressApiService.cancelarEnvio(anyLong())).thenReturn(true);

        // When
        envioService.eliminarEnvio(1);

        // Then
        verify(envioRepository, times(1)).findById(1);
        verify(chilexpressApiService, times(1)).cancelarEnvio(envio1.getIdEnvio());
        verify(envioRepository, times(1)).deleteById(1);
    }

    @Test
    void agregarProducto_debeAgregarProductoAEnvio() {
        // Given
        when(envioRepository.findById(1)).thenReturn(Optional.of(envio1));
        when(productoRepository.findById(p3.getIdProducto())).thenReturn(Optional.of(p3));

        Envio envioAfterAddition = new Envio();
        envioAfterAddition.setIdEnvio(envio1.getIdEnvio());
        envioAfterAddition.setEstadoPedido(envio1.getEstadoPedido());
        envioAfterAddition.setIdCliente(envio1.getIdCliente());
        envioAfterAddition.setFechaEnvio(envio1.getFechaEnvio());
        Set<Producto> productosTemp = new HashSet<>(envio1.getProductos());
        productosTemp.add(p3);
        envioAfterAddition.setProductos(productosTemp);

        when(envioRepository.save(any(Envio.class))).thenReturn(envioAfterAddition);

        // When
        Envio result = envioService.agregarProducto(1, p3.getIdProducto());

        // Then
        assertNotNull(result);
        assertTrue(result.getProductos().contains(p1));
        assertTrue(result.getProductos().contains(p2));
        assertTrue(result.getProductos().contains(p3));
        assertEquals(3, result.getProductos().size());
        verify(envioRepository, times(1)).findById(1);
        verify(productoRepository, times(1)).findById(p3.getIdProducto());
        verify(envioRepository, times(1)).save(envioAfterAddition);
    }

    @Test
    void cambiarEstado_debeCambiarEstadoYNotificarChilexpress() {
        // Given
        when(envioRepository.findById(1)).thenReturn(Optional.of(envio1));
        when(envioRepository.save(envio1)).thenReturn(envio1);
        when(chilexpressApiService.actualizarEstadoEnvio(anyLong(), anyString())).thenReturn(true);

        // When
        Envio result = envioService.cambiarEstado(1, Estado.ENTREGADO);

        // Then
        assertNotNull(result);
        assertEquals(Estado.ENTREGADO, result.getEstadoPedido());
        verify(envioRepository, times(1)).findById(1);
        verify(envioRepository, times(1)).save(envio1);
        verify(chilexpressApiService, times(1)).actualizarEstadoEnvio(envio1.getIdEnvio(), Estado.ENTREGADO.toString());
    }

    @Test
    void eliminarProductoDelEnvio_debeEliminarProductoDeEnvio() {
        // Given
        envio1.getProductos().clear();
        envio1.getProductos().add(p1);
        envio1.getProductos().add(p2);

        when(envioRepository.findById(1)).thenReturn(Optional.of(envio1));
        when(productoRepository.findById(p1.getIdProducto())).thenReturn(Optional.of(p1));

        Envio envioAfterRemoval = new Envio();
        envioAfterRemoval.setIdEnvio(envio1.getIdEnvio());
        envioAfterRemoval.setEstadoPedido(envio1.getEstadoPedido());
        envioAfterRemoval.setIdCliente(envio1.getIdCliente());
        envioAfterRemoval.setFechaEnvio(envio1.getFechaEnvio());
        Set<Producto> productosTemp = new HashSet<>();
        productosTemp.add(p2);
        envioAfterRemoval.setProductos(productosTemp);

        when(envioRepository.save(any(Envio.class))).thenReturn(envioAfterRemoval);


        // When
        Envio result = envioService.eliminarProducto(1, p1.getIdProducto());

        // Then
        assertNotNull(result);
        assertFalse(result.getProductos().contains(p1));
        assertTrue(result.getProductos().contains(p2));
        assertEquals(1, result.getProductos().size());
        verify(envioRepository, times(1)).findById(1);
        verify(productoRepository, times(1)).findById(p1.getIdProducto());
        verify(envioRepository, times(1)).save(envioAfterRemoval);
    }

    @Test
    void obtenerProductosDelEnvio_debeRetornarListaDeProductos() {
        // Given
        envio1.getProductos().clear();
        envio1.getProductos().add(p1);
        envio1.getProductos().add(p2);
        when(envioRepository.findById(1)).thenReturn(Optional.of(envio1));

        // When
        List<Producto> productos = envioService.obtenerProductosDelEnvio(1);

        // Then
        assertNotNull(productos);
        assertEquals(2, productos.size());
        assertTrue(productos.contains(p1));
        assertTrue(productos.contains(p2));
        verify(envioRepository, times(1)).findById(1);
    }

    @Test
    void obtenerProductosDelEnvio_debeRetornarListaVaciaSiEnvioNoExiste() {
        // Given
        when(envioRepository.findById(99)).thenReturn(Optional.empty());

        // When
        List<Producto> productos = envioService.obtenerProductosDelEnvio(99);

        // Then
        assertNotNull(productos);
        assertTrue(productos.isEmpty());
        verify(envioRepository, times(1)).findById(99);
    }
}