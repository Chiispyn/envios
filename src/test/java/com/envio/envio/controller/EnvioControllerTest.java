package com.envio.envio.controller;

import com.envio.envio.model.Envio;
import com.envio.envio.model.Estado;
import com.envio.envio.model.Producto;
import com.envio.envio.service.EnvioService;
import com.envio.envio.assemblers.EnvioModelAssembler; 
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import; 
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnvioController.class)
@Import(EnvioModelAssembler.class) // Importamos el EnvioModelAssembler para que esté disponible en el contexto del test
class EnvioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnvioService envioService;

    @Autowired
    private ObjectMapper objectMapper;

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
        envio1.setIdEnvio(1);
        envio1.setEstadoPedido(Estado.PENDIENTE);
        envio1.setIdCliente(101);
        envio1.setFechaEnvio(new Date());
        Set<Producto> productosEnvio1 = new HashSet<>();
        productosEnvio1.add(p1);
        productosEnvio1.add(p2);
        envio1.setProductos(productosEnvio1);

        envio2 = new Envio();
        envio2.setIdEnvio(2);
        envio2.setEstadoPedido(Estado.EN_CAMINO);
        envio2.setIdCliente(102);
        envio2.setFechaEnvio(new Date(System.currentTimeMillis() + 86400000));
        Set<Producto> productosEnvio2 = new HashSet<>();
        productosEnvio2.add(p3);
        envio2.setProductos(productosEnvio2);
    }

    @Test
    void obtenerTodosLosEnvios_debeRetornarListaConHATEOAS() throws Exception {
        when(envioService.listarEnvios()).thenReturn(Arrays.asList(envio1, envio2));

        mockMvc.perform(get("/api/v1/envios")
                        .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON_VALUE))
                // La colección de envíos ahora está dentro de "_embedded.envioList"
                .andExpect(jsonPath("$._embedded.envioList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.envioList[0].idEnvio", is(envio1.getIdEnvio())))
                .andExpect(jsonPath("$._embedded.envioList[0]._links.self.href", containsString("/api/v1/envios/" + envio1.getIdEnvio())))
                .andExpect(jsonPath("$._embedded.envioList[1].idEnvio", is(envio2.getIdEnvio())))
                .andExpect(jsonPath("$._embedded.envioList[1]._links.self.href", containsString("/api/v1/envios/" + envio2.getIdEnvio())))
                // Los enlaces de la colección global permanecen igual
                .andExpect(jsonPath("$._links.self.href", containsString("/api/v1/envios")))
                .andExpect(jsonPath("$._links.crear-envio.href", containsString("/api/v1/envios")));
        verify(envioService, times(1)).listarEnvios();
    }

    @Test
    void obtenerEnvio_debeRetornarEnvioConHATEOASCuandoExiste() throws Exception {
        when(envioService.obtenerEnvioPorId(1)).thenReturn(Optional.of(envio1));

        mockMvc.perform(get("/api/v1/envios/{id}", 1)
                        .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("$.idEnvio", is(envio1.getIdEnvio())))
                .andExpect(jsonPath("$.estadoPedido", is(envio1.getEstadoPedido().toString())))
                .andExpect(jsonPath("$._links.self.href", containsString("/api/v1/envios/1")))
                .andExpect(jsonPath("$._links.envios.href", containsString("/api/v1/envios")))
                .andExpect(jsonPath("$._links.marcar-entregado.href", containsString("/api/v1/envios/1/estado")))
                .andExpect(jsonPath("$._links.cancelar.href", containsString("/api/v1/envios/1/estado")))
                .andExpect(jsonPath("$._links.agregar-producto.href", containsString("/api/v1/envios/1/productos/")))
                .andExpect(jsonPath("$._links.eliminar-producto.href", containsString("/api/v1/envios/1/productos/")))
                .andExpect(jsonPath("$._links.productos-del-envio.href", containsString("/api/v1/envios/1/productos")));

        verify(envioService, times(1)).obtenerEnvioPorId(1);
    }

    @Test
    void obtenerEnvio_debeRetornarNotFoundCuandoNoExiste() throws Exception {
        when(envioService.obtenerEnvioPorId(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/envios/{id}", 99)
                        .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isNotFound());
        verify(envioService, times(1)).obtenerEnvioPorId(99);
    }

    @Test
    void crearEnvio_debeCrearEnvioYRetornarConHATEOAS() throws Exception {
        Envio nuevoEnvio = new Envio();
        nuevoEnvio.setEstadoPedido(Estado.PENDIENTE);
        nuevoEnvio.setIdCliente(103);
        nuevoEnvio.setFechaEnvio(new Date());
        nuevoEnvio.setProductos(new HashSet<>(Arrays.asList(p1)));

        Envio savedEnvio = new Envio();
        savedEnvio.setIdEnvio(3);
        savedEnvio.setEstadoPedido(Estado.PENDIENTE);
        savedEnvio.setIdCliente(103);
        savedEnvio.setFechaEnvio(nuevoEnvio.getFechaEnvio());
        savedEnvio.setProductos(nuevoEnvio.getProductos());


        when(envioService.guardarEnvio(any(Envio.class))).thenReturn(savedEnvio);

        mockMvc.perform(post("/api/v1/envios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoEnvio))
                        .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("$.idEnvio", is(savedEnvio.getIdEnvio())))
                .andExpect(jsonPath("$._links.self.href", containsString("/api/v1/envios/3")))
                .andExpect(jsonPath("$._links.productos-del-envio.href", containsString("/api/v1/envios/3/productos")));

        verify(envioService, times(1)).guardarEnvio(any(Envio.class));
    }

    @Test
    void actualizarEnvio_debeActualizarEnvioYRetornarConHATEOAS() throws Exception {
        Envio updatedInfo = new Envio();
        updatedInfo.setEstadoPedido(Estado.ENTREGADO);
        updatedInfo.setFechaEnvio(new Date());
        updatedInfo.setIdCliente(envio1.getIdCliente());

        Envio existingEnvioUpdated = new Envio();
        existingEnvioUpdated.setIdEnvio(1);
        existingEnvioUpdated.setEstadoPedido(Estado.ENTREGADO);
        existingEnvioUpdated.setIdCliente(envio1.getIdCliente());
        existingEnvioUpdated.setFechaEnvio(updatedInfo.getFechaEnvio());
        existingEnvioUpdated.setProductos(new HashSet<>(envio1.getProductos()));

        when(envioService.actualizarEnvio(eq(1), any(Envio.class))).thenReturn(existingEnvioUpdated);

        mockMvc.perform(put("/api/v1/envios/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedInfo))
                        .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON_VALUE))
                // Los campos de la entidad están en la raíz, los enlaces también
                .andExpect(jsonPath("$.idEnvio", is(existingEnvioUpdated.getIdEnvio())))
                .andExpect(jsonPath("$.estadoPedido", is(Estado.ENTREGADO.toString())))
                .andExpect(jsonPath("$._links.self.href", containsString("/api/v1/envios/1")));
        verify(envioService, times(1)).actualizarEnvio(eq(1), any(Envio.class));
    }

    @Test
    void actualizarEnvio_debeRetornarNotFoundSiEnvioNoExiste() throws Exception {
        Envio updatedInfo = new Envio();
        updatedInfo.setEstadoPedido(Estado.ENTREGADO);

        when(envioService.actualizarEnvio(eq(99), any(Envio.class))).thenReturn(null);

        mockMvc.perform(put("/api/v1/envios/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedInfo))
                        .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isNotFound());
        verify(envioService, times(1)).actualizarEnvio(eq(99), any(Envio.class));
    }

    @Test
    void eliminarEnvio_debeRetornarNoContent() throws Exception {
        doNothing().when(envioService).eliminarEnvio(1);

        mockMvc.perform(delete("/api/v1/envios/{id}", 1))
                .andExpect(status().isNoContent());
        verify(envioService, times(1)).eliminarEnvio(1);
    }

    @Test
    void agregarProductoAlEnvio_debeAgregarProductoYRetornarEnvioConHATEOAS() throws Exception {
        Envio envioAfterAddition = new Envio();
        envioAfterAddition.setIdEnvio(1);
        envioAfterAddition.setEstadoPedido(Estado.PENDIENTE);
        envioAfterAddition.setIdCliente(envio1.getIdCliente());
        envioAfterAddition.setFechaEnvio(envio1.getFechaEnvio());
        Set<Producto> productosTemp = new HashSet<>(envio1.getProductos());
        productosTemp.add(p3);
        envioAfterAddition.setProductos(productosTemp);

        when(envioService.agregarProducto(1, p3.getIdProducto())).thenReturn(envioAfterAddition);

        mockMvc.perform(post("/api/v1/envios/{idEnvio}/productos/{idProducto}", 1, p3.getIdProducto())
                        .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON_VALUE))
                // Los campos de la entidad están en la raíz, incluyendo 'productos'
                .andExpect(jsonPath("$.idEnvio", is(envioAfterAddition.getIdEnvio())))
                .andExpect(jsonPath("$.productos", hasSize(3)))
                // Para verificar un producto específico en la lista (sin DTOs, se accede directamente a sus campos)
                // Usamos `hasItems` para verificar la presencia de los nombres en la lista de productos
                .andExpect(jsonPath("$.productos[*].nombre", hasItems(p1.getNombre(), p2.getNombre(), p3.getNombre())))
                .andExpect(jsonPath("$._links.self.href", containsString("/api/v1/envios/1")));
        verify(envioService, times(1)).agregarProducto(1, p3.getIdProducto());
    }

    @Test
    void cambiarEstadoDelEnvio_debeCambiarEstadoYRetornarEnvioConHATEOAS() throws Exception {
        Envio updatedEnvio = new Envio();
        updatedEnvio.setIdEnvio(1);
        updatedEnvio.setEstadoPedido(Estado.ENTREGADO);
        updatedEnvio.setIdCliente(envio1.getIdCliente());
        updatedEnvio.setFechaEnvio(envio1.getFechaEnvio());
        updatedEnvio.setProductos(new HashSet<>(envio1.getProductos()));

        when(envioService.cambiarEstado(eq(1), eq(Estado.ENTREGADO))).thenReturn(updatedEnvio);

        mockMvc.perform(put("/api/v1/envios/{idEnvio}/estado", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Estado.ENTREGADO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON_VALUE))
                // Los campos de la entidad están en la raíz
                .andExpect(jsonPath("$.idEnvio", is(updatedEnvio.getIdEnvio())))
                .andExpect(jsonPath("$.estadoPedido", is(Estado.ENTREGADO.toString())))
                .andExpect(jsonPath("$._links.self.href", containsString("/api/v1/envios/1")))
                // Enlaces que ya no deberían aparecer si el estado es ENTREGADO
                .andExpect(jsonPath("$._links.marcar-entregado").doesNotExist())
                .andExpect(jsonPath("$._links.cancelar").doesNotExist())
                .andExpect(jsonPath("$._links.agregar-producto").doesNotExist())
                .andExpect(jsonPath("$._links.eliminar-producto").doesNotExist());
        verify(envioService, times(1)).cambiarEstado(eq(1), eq(Estado.ENTREGADO));
    }

    @Test
    void eliminarProductoDelEnvio_debeEliminarProductoYRetornarEnvioConHATEOAS() throws Exception {
        Envio envioAfterRemoval = new Envio();
        envioAfterRemoval.setIdEnvio(1);
        envioAfterRemoval.setEstadoPedido(Estado.PENDIENTE);
        envioAfterRemoval.setIdCliente(envio1.getIdCliente());
        envioAfterRemoval.setFechaEnvio(envio1.getFechaEnvio());
        Set<Producto> productosTemp = new HashSet<>();
        productosTemp.add(p2);
        envioAfterRemoval.setProductos(productosTemp);

        when(envioService.eliminarProducto(1, p1.getIdProducto())).thenReturn(envioAfterRemoval);

        mockMvc.perform(delete("/api/v1/envios/{idEnvio}/productos/{idProducto}", 1, p1.getIdProducto())
                        .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON_VALUE))
                // Los campos de la entidad están en la raíz
                .andExpect(jsonPath("$.idEnvio", is(envioAfterRemoval.getIdEnvio())))
                .andExpect(jsonPath("$.productos", hasSize(1)))
                // Acceso directo a los campos del producto restante
                .andExpect(jsonPath("$.productos[0].idProducto", is(p2.getIdProducto())))
                .andExpect(jsonPath("$._links.self.href", containsString("/api/v1/envios/1")));
        verify(envioService, times(1)).eliminarProducto(1, p1.getIdProducto());
    }

    @Test
    void obtenerProductosDelEnvio_debeRetornarListaDeProductos() throws Exception {
        List<Producto> productosDelEnvio1 = Arrays.asList(p1, p2);
        when(envioService.obtenerProductosDelEnvio(1)).thenReturn(productosDelEnvio1);

        mockMvc.perform(get("/api/v1/envios/{idEnvio}/productos", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Este endpoint no devuelve HATEOAS, así que el array de productos está en la raíz
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].idProducto", is(p1.getIdProducto())))
                .andExpect(jsonPath("$[1].idProducto", is(p2.getIdProducto())));
        verify(envioService, times(1)).obtenerProductosDelEnvio(1);
    }

     @Test
    void cambiarEstadoDelEnvio_debeRetornarNotFoundSiEnvioNoExiste() throws Exception {
        Estado nuevoEstado = Estado.ENTREGADO;
        when(envioService.cambiarEstado(eq(99), any(Estado.class))).thenReturn(null);

        mockMvc.perform(put("/api/v1/envios/{idEnvio}/estado", 99)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(nuevoEstado)))
                .andExpect(status().isNotFound());
        verify(envioService, times(1)).cambiarEstado(eq(99), any(Estado.class));
    }

    @Test
    void agregarProductoAlEnvio_debeRetornarNotFoundSiEnvioOProductoNoExisten() throws Exception {
        when(envioService.agregarProducto(eq(99), eq(p3.getIdProducto()))).thenReturn(null);

        mockMvc.perform(post("/api/v1/envios/{idEnvio}/productos/{idProducto}", 99, p3.getIdProducto())
                                .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isNotFound());
        verify(envioService, times(1)).agregarProducto(eq(99), eq(p3.getIdProducto()));
    }

    @Test
    void eliminarProductoDelEnvio_debeRetornarNotFoundSiEnvioOProductoNoExisten() throws Exception {
        when(envioService.eliminarProducto(eq(99), eq(p1.getIdProducto()))).thenReturn(null);

        mockMvc.perform(delete("/api/v1/envios/{idEnvio}/productos/{idProducto}", 99, p1.getIdProducto())
                                .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isNotFound());
        verify(envioService, times(1)).eliminarProducto(eq(99), eq(p1.getIdProducto()));
    }

    @Test
    void obtenerProductosDelEnvio_debeRetornarNotFoundSiEnvioNoExiste() throws Exception {
        when(envioService.obtenerProductosDelEnvio(99)).thenReturn(null);

        mockMvc.perform(get("/api/v1/envios/{idEnvio}/productos", 99)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(envioService, times(1)).obtenerProductosDelEnvio(99);
    }
}