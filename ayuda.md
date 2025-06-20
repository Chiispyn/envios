## llenar la bd
INSERT INTO producto (nombre, precio) VALUES ('Cepillo de dientes de bambú', 8.00);
INSERT INTO producto (nombre, precio) VALUES ('Shampoo sólido biodegradable', 15.00);
INSERT INTO producto (nombre, precio) VALUES ('Bolsa reutilizable de algodón orgánico', 12.00);
INSERT INTO producto (nombre, precio) VALUES ('Botella de agua de acero inoxidable', 25.00);
INSERT INTO producto (nombre, precio) VALUES ('Juego de cubiertos de bambú', 18.00);
INSERT INTO producto (nombre, precio) VALUES ('Esponja vegetal compostable', 5.00);
INSERT INTO producto (nombre, precio) VALUES ('Paños de cera de abeja reutilizables (set de 3)', 20.00);
INSERT INTO producto (nombre, precio) VALUES ('Jabón artesanal con ingredientes naturales', 10.00);
INSERT INTO producto (nombre, precio) VALUES ('Plato de madera de lenga', 35.00);
INSERT INTO producto (nombre, precio) VALUES ('Semillas orgánicas para huerto en casa (pack)', 15.00);

## Crear un nuevo envío (POST)
## http://localhost:8083/api/envios
{
  "fechaEnvio": "2025-05-16T11:00:00.000+00:00",
  "estadoPedido": "PENDIENTE",
  "idCliente": 456
}

## Obtener el envío creado (GET por ID)
## http://localhost:8083/api/envios/{idEnvio} por ejemplo 1

## http://localhost:8083/api/envios/{idEnvio}
## http://localhost:8083/api/envios/{idEnvio}/productos/1 (reemplaza {idEnvio} con tu ID)

## Obtener los productos del envío (GET)
## http://localhost:8083/api/envios/{idEnvio}/productos (e.g., http://localhost:8080/api/envios/3/productos).

## Actualizar el estado del envío (PUT)
## http://localhost:8083/api/envios/4
## Pega el siguiente JSON para cambiar el estado:
{
  "estadoPedido": "EN_CAMINO"
}

##  Eliminar un producto del envío (DELETE)
## http://localhost:8083/api/envios/3/productos/3

## Eliminar el envío (DELETE)
## http://localhost:8083/api/envios/3




{
  "idCliente": 123,
  "estadoPedido": "PENDIENTE",
  "fechaEnvio": "2025-06-17T14:30:00.000+00:00"
}