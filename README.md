# Motor Transaccional Core Bancario

Implementación de un núcleo transaccional bancario desarrollado bajo una arquitectura de Monolito Modular. El proyecto aborda los desafíos críticos del software financiero: integridad contable (ACID), control de concurrencia ante operaciones simultáneas, idempotencia y prevención activa de fraude.

## Arquitectura y Stack Tecnológico

* **Backend:** Java 21, Spring Boot 3, Spring Security, Spring Data JPA (Hibernate).
* **Base de Datos:** MySQL 8 (Motor transaccional InnoDB).
* **Frontend:** React (Vite), consumo de APIs RESTful desacopladas.
* **Contenerización:** Docker (Multi-stage build) para despliegues inmutables.

El backend se estructura siguiendo principios de diseño modular por dominio de negocio (`cuenta`, `transferencia`, `antifraude`, `seguridad`), permitiendo una futura extracción hacia microservicios independientes sin refactorización de la lógica de dominio.

## Características Técnicas de Nivel Bancario

### 1. Control de Concurrencia (Optimistic Locking)
Para evitar condiciones de carrera (*race conditions*) cuando dos procesos intentan debitar fondos de una misma cuenta simultáneamente, la entidad `Cuenta` implementa versionado mediante la anotación `@Version` de JPA. Ante una colisión, el sistema rechaza la segunda transacción con un estado HTTP `409 Conflict`, preservando la consistencia algorítmica del saldo.

### 2. Precisión Monetaria y Transaccionalidad ACID
* Todos los importes financieros se procesan utilizando `BigDecimal` con escala decimal fija (`DECIMAL(15,2)` en base de datos), mitigando los errores de redondeo de punto flotante (`Double`/`Float`).
* El procesamiento de transferencias se orquesta bajo contextos `@Transactional`, garantizando un *rollback* completo y automático ante cualquier fallo intermedio.

### 3. Idempotencia Operativa
Cada orden de transferencia incorpora un identificador único de operación (`codigoOperacion`) validado con restricción `UNIQUE` a nivel de base de datos, impidiendo débitos duplicados derivados de latencia de red o reintentos del cliente.

## Seguridad y Ciberseguridad Integrada

* **Spring Security & Criptografía:** La capa de autenticación está protegida mediante `SecurityFilterChain` bajo una política *Stateless* (sin estado), adecuada para APIs REST. Las credenciales de acceso se almacenan utilizando `BCryptPasswordEncoder`, asegurando un hash robusto con generación automática de *Salt* para mitigar ataques de tablas arcoíris (*Rainbow Tables*).
* **Motor Preventivo Antifraude:** Módulo desacoplado que evalúa reglas de negocio antes de autorizar movimientos de fondos. Incluye bloqueo de auto-transferencias y retención preventiva ante montos que superen el umbral de riesgo transaccional ($500.000 ARS).
* **Protección contra Fuerza Bruta:** Monitoreo transaccional de autenticación. El sistema bloquea de manera persistente la cuenta (`HTTP 423 Locked`) al registrar 3 intentos erróneos consecutivos.
* **Manejo Centralizado de Excepciones:** Implementación de `@RestControllerAdvice` (`GlobalExceptionHandler`) para aislar la lógica de manejo de errores, asegurar la auditoría interna mediante *logs* y estandarizar las respuestas HTTP sin filtrar trazas de la infraestructura al exterior.

## Estructura de Módulos (Backend)

```text
com.banco.backend_banco
 ├── antifraude          # Motor de reglas y evaluación de riesgo transaccional
 ├── cuenta              # Gestión de cuentas, saldos y control de versión
 ├── transferencia       # Orquestación transaccional e historial de auditoría
 ├── seguridad           # Spring Security, JWT/Tokens, y control de accesos
 └── GlobalExceptionHandler.java

## Despliegue y Ejecución en Entorno Local

### Requisitos Previos
* JDK 21 o superior.
* MySQL Server activo en el puerto `3306`.
* Node.js (v20+).

### 1. Base de Datos
Ejecutar el siguiente script DDL en la instancia de MySQL:
```sql
CREATE DATABASE banco_db;
```
*(Hibernate gestionará automáticamente el esquema de tablas. El componente `CommandLineRunner` inicializará los datos paramétricos).*

### 2. Backend (Spring Boot)
Configurar las credenciales en `src/main/resources/application.properties` y ejecutar:
```bash
mvn spring-boot:run
```

### 3. Frontend (React)
Desde el directorio `frontend-banco`, instalar dependencias e iniciar el servidor:
```bash
npm install
npm run dev
```
Acceder mediante interfaz web en `http://localhost:5173`.