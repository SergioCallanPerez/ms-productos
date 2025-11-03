# Microservicios- Productos y servicios

Proyecto que implementa dos microservicios para la gestión de pedidos utilizando **Springboot Webflux** y **R2dbc** para una conexión reactiva con
bases de datos y utilizando config-server para los perfiles de los microservicios.

---

## 1. Instalación

### Requisitos

- Java 21
- Gradle
- PostgreSQL

### Compilación del proyecto

Iniciar primero compilando ms-config-server.

- Productos: Ejecutar main.java

---

## 2. Base de datos

### Bases de datos
- CREATE DATABASE db_productos_dev
- CREATE DATABASE db_productos_prd
- CREATE DATABASE db_productos_qa

### Tablas
En cada base de datos, crear la tabla productos:
- CREATE TABLE productos (
  id BIGSERIAL PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL,
  descripcion TEXT,
  precio NUMERIC(10, 2) NOT NULL CHECK (precio >= 0),
  stock INTEGER NOT NULL CHECK (stock >= 0),
  activo BOOLEAN DEFAULT TRUE,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  );

### Procedimientos
Crear en cada base de datos:
- CREATE OR REPLACE FUNCTION actualizar_stock(
  p_producto_id BIGINT,
  p_cantidad INTEGER
  ) RETURNS VOID AS $$
  DECLARE
  v_stock_actual INTEGER;
  BEGIN
  SELECT stock INTO v_stock_actual FROM productos WHERE id = p_producto_id;

  IF NOT FOUND THEN
  RAISE EXCEPTION 'Producto con id % no encontrado', p_producto_id;
  END IF;

  IF v_stock_actual < p_cantidad THEN
  RAISE EXCEPTION 'Stock insuficiente para el producto %', p_producto_id;
  END IF;

  UPDATE productos
  SET stock = stock - p_cantidad
  WHERE id = p_producto_id;
  END;
  $$ LANGUAGE plpgsql;
- CREATE OR REPLACE FUNCTION productos_bajo_stock(
  p_minimo INTEGER
  ) RETURNS TABLE(
  id BIGINT,
  nombre VARCHAR,
  stock INTEGER
  ) AS \$\$
  BEGIN
  RETURN QUERY
  SELECT p.id, p.nombre, p.stock
  FROM productos p
  WHERE p.stock < p_minimo AND p.activo = true;
  END;
  \$\$ LANGUAGE plpgsql;

---
## 3. Cambios en el código
Crear y/o cambiar el .env de cada microservicio para la nueva url de las bases de datos; ejemplo:
- DB_URL_PRODUCTOS_DEV=r2dbc:postgresql://localhost:5432/db_productos_dev
- DB_URL_PRODUCTOS_PRD=r2dbc:postgresql://localhost:5432/db_productos_prd
- DB_URL_PRODUCTOS_QA=r2dbc:postgresql://localhost:5432/db_productos_qa
- DB_USERNAME=postgres
- DB_PASSWORD=admin

---

## 4. Endpoints y sus usos

URL base: /api/productos
### Ver productos: GET /

Response:

[
{
"id": 3,
"nombre": "Teclado Mecánico",
"descripcion": "Teclado RGB",
"precio": 80.0,
"stock": 30,
"activo": true,
"fechaCreacion": [
2025,11,1,19,55,37,862263000
]
},
{
"id": 2,
"nombre": "Mouse Logitech",
"descripcion": "Mouse inalámbrico",
"precio": 25.0,
"stock": 49,
"activo": true,
"fechaCreacion": [
2025,11,1,19,55,37,862263000
]
},
{
"id": 1,
"nombre": "Laptop Dell",
"descripcion": "Laptop i7 16GB RAM",
"precio": 1200.0,
"stock": 8,
"activo": true,
"fechaCreacion": [
2025,11,1,19,55,37,862263000
]
}
]

### Ver producto en específico: GET /{id}
Response:
{
"id": 1,
"nombre": "Laptop Dell",
"descripcion": "Laptop i7 16GB RAM",
"precio": 1200.0,
"stock": 8,
"activo": true,
"fechaCreacion": [
2025,11,1,19,55,37,862263000
]
}

### Ver productos con bajo stock: GET /{stock}
Response:

[
{
"id": 1,
"nombre": "Laptop Dell",
"stock": 8
},
{
"id": 6,
"nombre": "Laptop Dell",
"stock": 0
},
{
"id": 5,
"nombre": "Laptop Dell",
"stock": 0
}
]


### Creación de productos: POST /

Request:

{
"nombre": "Laptop Dell",
"descripcion": "Laptop i7 16GB RAM",
"precio": 1250.0,
"stock": 20,
"activo": true,
"fechaCreacion": [
2025,11,1,19,55,37,862263000
]
}

Response (201):

{
"id": 8,
"nombre": "Laptop Dell",
"descripcion": "Laptop i7 16GB RAM",
"precio": 1250.0,
"stock": 20,
"activo": true,
"fechaCreacion": [
2025,11,1,19,55,37,862263000
]
}

### Actualización de productos: PUT /{id}

Request:

{
"nombre": "Laptop Dell",
"descripcion": "Laptop i7 16GB RAM",
"precio": 1250.0,
"stock": 10,
"activo": true,
"fechaCreacion": [
2025,11,1,19,55,37,862263000
]
}

Response:

{
"id": 8,
"nombre": "Laptop Dell",
"descripcion": "Laptop i7 16GB RAM",
"precio": 1250.0,
"stock": 10,
"activo": true,
"fechaCreacion": [
2025,11,1,19,55,37,862263000
]
}

### Borrado de un producto: DELETE /{id}

### Actualizar stock (pensada para no ser utilizada directamente): PATCH /{id}/stock
Request:

{
"cantidad": 1
}
