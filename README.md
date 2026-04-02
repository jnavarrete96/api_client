# API Client

REST API reactiva para gestión de clientes, construida con Spring WebFlux y arquitectura hexagonal.

---

## Tecnologías

- Java 21
- Spring Boot 3.4.1
- Spring WebFlux (reactor)
- Spring Data R2DBC
- H2 (base de datos en memoria)
- Lombok
- Jakarta Validation
- JUnit 5 + Mockito + StepVerifier

---

## Arquitectura

El proyecto sigue **arquitectura hexagonal (ports & adapters)**:

```
com.api.client
├── adapter
│   ├── in.web          # Controllers, DTOs de entrada/salida, manejo de errores
│   └── out.persistence # Repositorio R2DBC, entidades, queries dinámicas
├── config              # BeanConfig, DataLoader
└── domain
    ├── exception        # Excepciones de dominio
    ├── model            # Modelos de dominio
    ├── port
    │   ├── in           # ClientUseCase (puerto de entrada)
    │   └── out          # ClientRepositoryPort (puerto de salida)
    └── service          # ClientService (lógica de negocio)
```

El dominio no conoce ni depende de Spring, R2DBC ni ningún framework — solo de interfaces propias.

---

## Requisitos

- Java 21+
- Gradle 8+

No se requiere ninguna base de datos instalada — usa H2 en memoria.

---

## Cómo ejecutar

```bash
./gradlew bootRun
```

La API queda disponible en `http://localhost:8080`.

Al arrancar, el `DataLoader` inserta automáticamente 5 clientes de prueba.

---

## Cómo ejecutar los tests

```bash
./gradlew test
```

Los tests están organizados en tres capas:

| Clase | Tipo | Descripción |
|---|---|---|
| `ClientServiceTest` | Unitario | Lógica de dominio con Mockito |
| `ClientControllerTest` | Slice web | Endpoints con `@WebFluxTest` |
| `ClientRepositoryAdapterTest` | Integración | Persistencia real con H2 |

---

## Endpoints

### Obtener todos los clientes

```
GET /api/clients
```

**Response:**
```json
{
  "timestamp": "2026-04-01T19:36:41",
  "status": 200,
  "message": "Clients retrieved successfully",
  "count": 5,
  "data": [...]
}
```

---

### Obtener cliente por sharedKey

```
GET /api/clients/{sharedKey}
```

**Response:**
```json
{
  "status": 200,
  "message": "Client found",
  "count": 1,
  "data": [{ ... }]
}
```

---

### Crear cliente

```
POST /api/clients
Content-Type: application/json
```

**Request body:**
```json
{
  "name": "Ana Garcia",
  "email": "ana.garcia@mail.com",
  "phone": "3001234567",
  "startDate": "2024-01-15",
  "endDate": "2025-01-15"
}
```

**Validaciones:**
- `name`: requerido, entre 2 y 100 caracteres, solo letras y espacios
- `email`: requerido, formato válido
- `endDate`: si se envía, debe ser igual o posterior a `startDate`
- El email no puede estar duplicado

**Response `201 Created`:**
```json
{
  "status": 200,
  "message": "Client created successfully",
  "count": 1,
  "data": [{ ... }]
}
```

---

### Búsqueda avanzada

```
POST /api/clients/search
Content-Type: application/json
```

Todos los campos son opcionales. Soporta búsqueda parcial por `sharedKey`, `name` y `email` (case-insensitive). Los filtros de fecha aplican como rango sobre `start_date` y `end_date`.

**Request body:**
```json
{
  "sharedKey": "ana",
  "name": "Garcia",
  "email": "mail.com",
  "startDate": "2024-01-01",
  "endDate": "2025-12-31",
  "page": 0,
  "size": 10
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Search completed successfully",
  "count": 1,
  "data": [{ ... }]
}
```

---

### Exportar a CSV

```
GET /api/clients/export
Accept: text/plain
```

**Response:**
```
SharedKey,BusinessId,Email,Phone,StartDate,EndDate,DateAdded
ana_garcia_a1b2c3,Ana Garcia,ana.garcia@mail.com,3001234567,2024-01-15,2025-01-15,2026-04-01
```

---

## Manejo de errores

Todas las respuestas de error siguen la misma estructura:

```json
{
  "status": 409,
  "message": "Client with email already exists: ana.garcia@mail.com",
  "timestamp": "2026-04-01T19:06:13"
}
```

| Excepción | HTTP |
|---|---|
| `DuplicateClientException` | 409 Conflict |
| `InvalidDateRangeException` | 400 Bad Request |
| `WebExchangeBindException` (validaciones) | 400 Bad Request |
| `DataIntegrityViolationException` | 409 Conflict |
| `Exception` (fallback) | 500 Internal Server Error |

---

## Esquema de base de datos

```sql
CREATE TABLE IF NOT EXISTS client (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    shared_key VARCHAR(255) NOT NULL,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    phone      VARCHAR(50),
    start_date DATE         NOT NULL,
    end_date   DATE,
    data_added DATE         NOT NULL,
    CONSTRAINT uk_shared_key UNIQUE (shared_key),
    CONSTRAINT uk_email      UNIQUE (email)
);
```

El `shared_key` se genera automáticamente al crear un cliente con el formato `nombre_apellido_xxxxxx`.

---

## Datos de prueba

Al iniciar la aplicación se cargan automáticamente los siguientes clientes:

| sharedKey | Email | StartDate | EndDate |
|---|---|---|---|
| ana_garcia_a1b2c3 | ana.garcia@mail.com | 2024-01-15 | 2025-01-15 |
| carlos_lopez_d4e5f6 | carlos.lopez@mail.com | 2024-03-10 | — |
| maria_torres_g7h8i9 | maria.torres@mail.com | 2024-06-01 | 2026-06-01 |
| juan_perez_j1k2l3 | juan.perez@mail.com | 2023-11-20 | — |
| laura_martinez_m4n5o6 | laura.martinez@mail.com | 2024-09-05 | 2025-09-05 |