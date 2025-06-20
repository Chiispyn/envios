package com.envio.envio.controller;

import com.envio.envio.model.Producto;
import com.envio.envio.service.ProductoService;
import com.envio.envio.assemblers.ProductoModelAssembler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
@Import(ProductoModelAssembler.class)
@ActiveProfiles("test") // Activa el perfil 'test' aquí
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper;

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
    void obtenerTodosLosProductos_debeRetornarListaConHATEOAS() throws Exception {
        // Given
        when(productoService.listarProductos()).thenReturn(Arrays.asList(producto1, producto2));

        // When & Then
        mockMvc.perform(get("/api/productos")
                        .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("$._embedded.productoList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.productoList[0].idProducto", is(producto1.getIdProducto())))
                .andExpect(jsonPath("$._embedded.productoList[0]._links.self.href", containsString("/api/productos/" + producto1.getIdProducto())))
                .andExpect(jsonPath("$._embedded.productoList[1].idProducto", is(producto2.getIdProducto())))
                .andExpect(jsonPath("$._embedded.productoList[1]._links.self.href", containsString("/api/productos/" + producto2.getIdProducto())))
                .andExpect(jsonPath("$._links.self.href", containsString("/api/productos")))
                .andExpect(jsonPath("$._links.crear-producto.href", containsString("/api/productos")));
        verify(productoService, times(1)).listarProductos();
    }

    @Test
    void obtenerProducto_debeRetornarProductoConHATEOASCuandoExiste() throws Exception {
        // Given
        when(productoService.obtenerProductoPorId(1)).thenReturn(Optional.of(producto1));

        // When & Then
        mockMvc.perform(get("/api/productos/{id}", 1)
                        .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("$.idProducto", is(producto1.getIdProducto())))
                .andExpect(jsonPath("$.nombre", is(producto1.getNombre())))
                .andExpect(jsonPath("$._links.self.href", containsString("/api/productos/1")))
                .andExpect(jsonPath("$._links.productos.href", containsString("/api/productos")));
        verify(productoService, times(1)).obtenerProductoPorId(1);
    }

    @Test
    void obtenerProducto_debeRetornarNotFoundCuandoNoExiste() throws Exception {
        // Given
        when(productoService.obtenerProductoPorId(99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/productos/{id}", 99)
                        .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isNotFound());
        verify(productoService, times(1)).obtenerProductoPorId(99);
    }

    @Test
    void crearProducto_debeCrearProductoYRetornarConHATEOAS() throws Exception {
        // Given
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre("Shampoo sólido biodegradable"); // Un producto que podría ser nuevo
        nuevoProducto.setPrecio(5000.0);

        Producto savedProducto = new Producto();
        savedProducto.setIdProducto(3); // Simula el ID que se le asignaría al guardarse
        savedProducto.setNombre("Shampoo sólido biodegradable");
        savedProducto.setPrecio(5000.0);

        when(productoService.guardarProducto(any(Producto.class))).thenReturn(savedProducto);

        // When & Then
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoProducto))
                        .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("$.idProducto", is(savedProducto.getIdProducto())))
                .andExpect(jsonPath("$.nombre", is(savedProducto.getNombre())))
                .andExpect(jsonPath("$._links.self.href", containsString("/api/productos/3")))
                .andExpect(jsonPath("$._links.productos.href", containsString("/api/productos")));
        verify(productoService, times(1)).guardarProducto(any(Producto.class));
    }

    @Test
    void actualizarProducto_debeActualizarProductoYRetornarConHATEOAS() throws Exception {
        // Given
        Producto updatedInfo = new Producto();
        updatedInfo.setNombre("Plato de madera de roble"); // Actualizamos el nombre de producto1
        updatedInfo.setPrecio(4000.0);

        Producto existingProductUpdated = new Producto();
        existingProductUpdated.setIdProducto(1);
        existingProductUpdated.setNombre("Plato de madera de roble");
        existingProductUpdated.setPrecio(4000.0);

        when(productoService.actualizarProducto(eq(1), any(Producto.class))).thenReturn(existingProductUpdated);

        // When & Then
        mockMvc.perform(put("/api/productos/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedInfo))
                        .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("$.idProducto", is(existingProductUpdated.getIdProducto())))
                .andExpect(jsonPath("$.nombre", is(existingProductUpdated.getNombre())))
                .andExpect(jsonPath("$._links.self.href", containsString("/api/productos/1")));
        verify(productoService, times(1)).actualizarProducto(eq(1), any(Producto.class));
    }

    @Test
    void actualizarProducto_debeRetornarNotFoundSiProductoNoExiste() throws Exception {
        // Given
        Producto updatedInfo = new Producto();
        updatedInfo.setNombre("Producto Inexistente");

        when(productoService.actualizarProducto(eq(99), any(Producto.class))).thenReturn(null);

        // When & Then
        mockMvc.perform(put("/api/productos/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedInfo))
                        .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isNotFound());
        verify(productoService, times(1)).actualizarProducto(eq(99), any(Producto.class));
    }

    @Test
    void eliminarProducto_debeRetornarNoContent() throws Exception {
        // Given
        doNothing().when(productoService).eliminarProducto(1);

        // When & Then
        mockMvc.perform(delete("/api/productos/{id}", 1))
                .andExpect(status().isNoContent());
        verify(productoService, times(1)).eliminarProducto(1);
    }
}