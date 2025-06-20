package com.envio.envio.service; // O el paquete donde tengas tu DataLoader

import com.envio.envio.model.Envio;
import com.envio.envio.model.Estado;
import com.envio.envio.model.Producto;
import com.envio.envio.repository.EnvioRepository;
import com.envio.envio.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile; // Importar Profile
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
@Profile("!test") // ¡Este DataLoader se ejecutará solo cuando el perfil 'test' NO esté activo!
public class DataLoader implements CommandLineRunner {

    private final ProductoRepository productoRepository;
    private final EnvioRepository envioRepository;

    public DataLoader(ProductoRepository productoRepository, EnvioRepository envioRepository) {
        this.productoRepository = productoRepository;
        this.envioRepository = envioRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Limpiar las tablas para asegurar un estado fresco si se reinicia la aplicación
        // ¡OJO! Esto es útil para desarrollo, pero no lo usarías en producción sin un plan de migración de datos.
        envioRepository.deleteAll();
        productoRepository.deleteAll();

        // 1. Crear Productos
        Producto p1 = new Producto();
        p1.setNombre("Plato de madera de lenga");
        p1.setPrecio(3500.00);
        productoRepository.save(p1);

        Producto p2 = new Producto();
        p2.setNombre("Juego de cubiertos de bambú");
        p2.setPrecio(4500.00);
        productoRepository.save(p2);

        Producto p3 = new Producto();
        p3.setNombre("Shampoo sólido biodegradable");
        p3.setPrecio(5000.00);
        productoRepository.save(p3);

        Producto p4 = new Producto();
        p4.setNombre("Cepillo de dientes de bambú");
        p4.setPrecio(2000.00);
        productoRepository.save(p4);

        Producto p5 = new Producto();
        p5.setNombre("Bolsa de tela reutilizable");
        p5.setPrecio(2500.00);
        productoRepository.save(p5);

        // 2. Crear Envíos
        // Envío 1: PENDIENTE, con productos p1 y p2
        Envio envio1 = new Envio();
        envio1.setEstadoPedido(Estado.PENDIENTE);
        envio1.setIdCliente(101);
        envio1.setFechaEnvio(new Date()); // Fecha actual
        Set<Producto> productosEnvio1 = new HashSet<>();
        productosEnvio1.add(p1);
        productosEnvio1.add(p2);
        envio1.setProductos(productosEnvio1);
        envioRepository.save(envio1);

        // Envío 2: EN_CAMINO, con producto p3
        Envio envio2 = new Envio();
        envio2.setEstadoPedido(Estado.EN_CAMINO);
        envio2.setIdCliente(102);
        // Fecha futura para simular que aún no ha llegado
        envio2.setFechaEnvio(new Date(System.currentTimeMillis() + 86400000)); // Mañana
        Set<Producto> productosEnvio2 = new HashSet<>();
        productosEnvio2.add(p3);
        envio2.setProductos(productosEnvio2);
        envioRepository.save(envio2);

        // Envío 3: ENTREGADO, con productos p4 y p5
        Envio envio3 = new Envio();
        envio3.setEstadoPedido(Estado.ENTREGADO);
        envio3.setIdCliente(103);
        // Fecha pasada para simular que ya se entregó
        envio3.setFechaEnvio(new Date(System.currentTimeMillis() - 172800000)); // Hace dos días
        Set<Producto> productosEnvio3 = new HashSet<>();
        productosEnvio3.add(p4);
        productosEnvio3.add(p5);
        envio3.setProductos(productosEnvio3);
        envioRepository.save(envio3);

        // Envío 4: CANCELADO, sin productos (o con un set vacío)
        Envio envio4 = new Envio();
        envio4.setEstadoPedido(Estado.CANCELADO);
        envio4.setIdCliente(104);
        envio4.setFechaEnvio(new Date());
        envio4.setProductos(new HashSet<>()); // Un envío cancelado podría no tener productos asociados, o el pedido fue cancelado antes de asignar productos.
        envioRepository.save(envio4);


        System.out.println("Datos iniciales cargados exitosamente.");
    }
}