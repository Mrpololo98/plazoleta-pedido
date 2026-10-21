# Microservicio de Pedidos · Plazoleta de Comidas

API del reto Plazoleta encargada del ciclo de vida completo de los pedidos:
desde que un cliente arma su orden con los platos de un restaurante (HU11),
pasando por la gestión del empleado (HU12 y HU13), el aviso de pedido listo
con pin de seguridad notificado por SMS (HU14), la entrega verificada por
pin (HU15) y la cancelación (HU16), hasta la trazabilidad de cada cambio de
estado (HU17) y las métricas de eficiencia para el dueño del restaurante
(HU18). Proyecto educativo construido con **Spring Boot 4.1.1** y
arquitectura hexagonal; valida los JWT que emite el microservicio de
usuarios y colabora con los microservicios de usuarios y restaurantes por
Feign.

## Endpoints

| Método | Ruta | Acceso | Historia |
|---|---|---|---|
| `POST` | `/pedidos` | Token **CLIENTE** | HU11 · realiza un pedido (platos de un mismo restaurante) |
| `GET` | `/pedidos?estado=&pagina=&cantidad=` | Token **EMPLEADO** | HU12 · lista los pedidos por estado (solo de su restaurante) |
| `PATCH` | `/pedidos/{id}/asignar` | Token **EMPLEADO** | HU13 · se asigna el pedido y pasa a en preparación |
| `PATCH` | `/pedidos/{id}/listo` | Token **EMPLEADO** | HU14 · pedido listo: genera pin y notifica SMS al cliente |
| `PATCH` | `/pedidos/{id}/entregar` | Token **EMPLEADO** | HU15 · entrega con el pin de seguridad |
| `PATCH` | `/pedidos/{id}/cancelar` | Token **CLIENTE** | HU16 · cancela un pedido pendiente propio |
| `GET` | `/pedidos/{id}/trazabilidad` | Token **CLIENTE** | HU17 · historial de estados de su pedido |
| `GET` | `/pedidos/eficiencia?idRestaurante=` | Token **PROPIETARIO** | HU18 · duración por pedido y ranking de empleados |

Las peticiones protegidas viajan con el encabezado `Authorization: Bearer <token>`.

## Máquina de estados del pedido

```
PENDIENTE ──asignar──> EN_PREPARACION ──listo──> LISTO ──entregar(pin)──> ENTREGADO
    │
    └──cancelar──> CANCELADO
```

- Un pedido nace `PENDIENTE` y el cliente no puede abrir otro mientras
  tenga uno en proceso (`PENDIENTE`, `EN_PREPARACION` o `LISTO`).
- El pin de seguridad se guarda cifrado con BCrypt; el SMS se simula en la
  bitácora del microservicio (cambiar `NotificadorSmsBitacora` por un
  proveedor real no toca el dominio).
- En entorno de demo la respuesta de `.../listo` incluye el pin para poder
  ejercitar la entrega sin acceso al teléfono del cliente.

## Cuentas demo

Este microservicio no siembra datos propios: usa las cuentas del
microservicio de usuarios (administrador, propietarios Camila id 2 y Jorge
id 3, cliente Valentina id 4) y los restaurantes del microservicio de
restaurantes (La Brasa Dorada id 1, Wok Express id 2).

## Pila tecnológica

| Componente | Detalle |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 4.1.1 (Web MVC, Data JPA, Security, Validation) |
| Documentación | springdoc-openapi 3.1.1 · Swagger UI |
| Base de datos | MySQL 8 |
| Seguridad | JJWT 0.12.6 (valida el JWT de usuarios) · BCrypt para el pin |
| Integración | OpenFeign → usuarios (celular del cliente) y restaurantes (carta, vínculo, dueño) |
| Mensajería | Puerto `NotificadorSmsPuerto` con adaptador de bitácora (Twilio/SNS en producción) |
| Build | Maven 3.9 (wrapper incluido) |

## Requisitos

- JDK 17
- MySQL 8 (contenedor Docker recomendado)
- Los microservicios de usuarios (8080) y restaurantes (8081) corriendo
- Maven 3.9 o superior, o usar el wrapper incluido (`./mvnw`)

## Base de datos

```powershell
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS plazoleta_pedido;"
```

Contenedor Docker sugerido:

```powershell
docker run --name mysql-plazoleta -e MYSQL_ROOT_PASSWORD=root -e MYSQL_USER=plazoleta -e MYSQL_PASSWORD=plazoleta -p 3306:3306 -d mysql:8.0
```

## Variables de entorno

| Variable | Uso |
|---|---|
| `DB_URL` | Cadena JDBC hacia la base de datos `plazoleta_pedido` |
| `DB_USERNAME` | Usuario de la base de datos |
| `DB_PASSWORD` | Clave de la base de datos |
| `APP_JWT_SECRET` | Secreto compartido con los otros microservicios (mínimo 32 caracteres) |
| `USUARIO_API_URL` | Opcional: URL del micro de usuarios (por defecto `http://localhost:8080`) |
| `RESTAURANTE_API_URL` | Opcional: URL del micro de restaurantes (por defecto `http://localhost:8081`) |

## Ejecución local

```bash
./mvnw spring-boot:run
```

La API queda disponible en el puerto `8082`.

## Ejecución con Docker

```powershell
docker build -t ms-pedidos .
docker run -p 8082:8082 --env DB_URL="jdbc:mysql://host.docker.internal:3306/plazoleta_pedido" --env DB_USERNAME=plazoleta --env DB_PASSWORD=plazoleta --env APP_JWT_SECRET="tu-secreto-compartido-de-32-caracteres-minimo" --env USUARIO_API_URL="http://host.docker.internal:8080" --env RESTAURANTE_API_URL="http://host.docker.internal:8081" ms-pedidos
```

## Documentación interactiva

Swagger UI disponible en: `http://localhost:8082/swagger-ui/index.html`

## Pruebas

Las pruebas unitarias usan H2 en memoria, no requieren MySQL ni los otros
microservicios:

```bash
./mvnw test
```

## Estructura del proyecto

```
com.plazoleta.pedido
├── domain
│   ├── api              # contratos de casos de uso (HU11 a HU18)
│   ├── exception        # excepciones de negocio
│   ├── model            # Pedido, EstadoPedido, Trazabilidad, Eficiencia
│   └── spi              # puertos de salida: repositorios, remotos, SMS, cifrador
├── application
│   ├── dto              # objetos de transferencia
│   └── service          # implementación de casos de uso
└── infrastructure
    ├── configuration    # configuración de OpenAPI
    ├── integraciones    # Feign hacia usuarios y restaurantes
    ├── notificacion     # SMS simulado en bitácora y cifrador del pin
    ├── persistence      # JPA: entidades, repositorios, adaptadores
    ├── rest             # controladores, DTOs REST y manejo de errores
    └── security         # validación del JWT compartido y reglas por rol
```

## Autores

- **Isaac Herrera**
- **Cesar Orozco**
