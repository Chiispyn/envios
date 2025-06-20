package com.envio.envio.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
public class Envio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEnvio;

    @Enumerated(EnumType.STRING)
    private Estado estadoPedido;
    private Integer idCliente;

    @Temporal(TemporalType.TIMESTAMP) 
    private Date fechaEnvio;

    @ManyToMany
    @JoinTable(
            name = "envio_producto",
            joinColumns = @JoinColumn(name = "id_envio"),
            inverseJoinColumns = @JoinColumn(name = "id_producto")
    )
    private Set<Producto> productos = new HashSet<>();
}