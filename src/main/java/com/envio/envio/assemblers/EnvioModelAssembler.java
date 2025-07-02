package com.envio.envio.assemblers;

import com.envio.envio.controller.EnvioController;
import com.envio.envio.model.Envio;
import com.envio.envio.model.Estado;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull; 

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class EnvioModelAssembler implements RepresentationModelAssembler<Envio, EntityModel<Envio>> {


    @Override
    public @NonNull EntityModel<Envio> toModel(@NonNull Envio envio) {
        EntityModel<Envio> envioModel = EntityModel.of(envio,
                linkTo(methodOn(EnvioController.class).obtenerEnvio(envio.getIdEnvio())).withSelfRel(),
                linkTo(methodOn(EnvioController.class).obtenerTodosLosEnvios()).withRel("envios"),
                linkTo(methodOn(EnvioController.class).obtenerProductosDelEnvio(envio.getIdEnvio())).withRel("productos-del-envio")
        );

        if (envio.getEstadoPedido() == Estado.PENDIENTE || envio.getEstadoPedido() == Estado.EN_CAMINO) {
            envioModel.add(linkTo(methodOn(EnvioController.class).cambiarEstadoDelEnvio(envio.getIdEnvio(), Estado.ENTREGADO))
                    .withRel("marcar-entregado"));
            envioModel.add(linkTo(methodOn(EnvioController.class).cambiarEstadoDelEnvio(envio.getIdEnvio(), Estado.CANCELADO))
                    .withRel("cancelar"));
            envioModel.add(linkTo(methodOn(EnvioController.class).agregarProductoAlEnvio(envio.getIdEnvio(), null))
                    .withRel("agregar-producto"));
            envioModel.add(linkTo(methodOn(EnvioController.class).eliminarProductoDelEnvio(envio.getIdEnvio(), null))
                    .withRel("eliminar-producto"));
        }

        return envioModel;
    }
}