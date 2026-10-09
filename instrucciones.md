# Instrucciones para ejecutar y probar cada componente

## Requisitos previos

- JDK compatible con cada proyecto y Maven o Maven Wrapper.
- Docker Desktop y Docker Compose para Parte 3.
- MySQL disponible para los servicios que usan la base `banco_bff`.
- Postman.
- Conectividad autorizada hacia Kafka en AWS EC2 para pruebas asíncronas.

Los comandos de Maven Wrapper se muestran para CMD de Windows.
En Linux/macOS utiliza `./mvnw` en lugar de `mvnw.cmd`.

## 1. Ejecutar Parte 1: Spring Batch

1. Abre `Parte 1/banco_sociedad`.
2. Revisa los CSV en `src/main/resources/data`.
3. Selecciona un Job mediante `spring.batch.job.name` en
   `src/main/resources/application.properties`.

Ejemplo:

```properties
spring.batch.job.name=dailyTransactionJob
```

Otras opciones, una por ejecución:

```properties
spring.batch.job.name=quarterlyInterestJob
spring.batch.job.name=annualStatementJob
```

4. Desde la carpeta del proyecto, ejecuta:

```cmd
mvnw.cmd spring-boot:run
```

5. Comprueba el estado `COMPLETED`, los contadores y los datos persistidos
   en H2 según la configuración del proyecto.

Resultados de referencia: 480 transacciones válidas de 1.000 entradas;
396 intereses persistidos y dos duplicados descartados.

## 2. Ejecutar Parte 2: BFF

Inicia desde IntelliJ o desde consola, en este orden:

1. `Parte 2/bank_core_api` (8180).
2. `Parte 2/web_bff` (8181).
3. `Parte 2/mobile_bff` (8182).
4. `Parte 2/atm_bff` (8183).

Desde cada carpeta:

```cmd
mvnw.cmd spring-boot:run
```

### Autenticación

```http
POST http://localhost:8180/auth/login
Content-Type: application/json

{"username":"webuser","password":"<CLAVE_DE_PRUEBA>"}
```

Repite con las credenciales del entorno de prueba correspondientes a
los otros canales. Utiliza el JWT obtenido como
`Authorization: Bearer <TOKEN>`.

### Endpoints principales

```http
GET  http://localhost:8180/api/accounts
GET  http://localhost:8181/web/accounts
GET  http://localhost:8182/mobile/accounts
GET  http://localhost:8183/atm/accounts
POST http://localhost:8183/atm/accounts/withdraw
```

Ejemplo de retiro:

```json
{"accountId":1001,"amount":50000}
```

### Pruebas sugeridas

- JWT WEB: acceso autorizado a `/web/accounts`.
- JWT MOBILE: respuesta compacta desde `/mobile/accounts`.
- JWT ATM: consulta y retiro desde `/atm`.
- Sin token: `401 Unauthorized` en rutas protegidas.
- Token de otro canal: `403 Forbidden` en rutas con scopes distintos.

**Ajuste pendiente de colección:** la solicitud de login MOBILE
en el JSON de Postman utiliza `localhost:8080`; en la arquitectura
actual `bank_core_api` escucha en `8180`. El README anterior de Parte 2
también contiene una ruta ATM diferente a la colección: toma como
referencia el mapping Java final o actualiza la colección.

## 3. Ejecutar Parte 3: Spring Cloud

### Preparación

1. Comprueba que MySQL esté iniciado y que exista `banco_bff`.
2. Verifica que la instancia EC2 y los brokers Kafka estén activos.
3. Detén en IntelliJ las aplicaciones que puedan ocupar los puertos
   8761, 8888, 9000 y 8081–8084.
4. Desde `Parte 3`, inicia Docker:

```cmd
docker compose up -d
docker compose ps
```

5. Comprueba los siete contenedores. `Up` no garantiza por sí solo
   que cada integración esté funcionando.
6. Abre Eureka: http://localhost:8761
7. Comprueba Config Server:

```http
GET http://localhost:8888/cuenta-service/default
```

### OAuth2

Solicitud de token (cliente y secreto configurados en el entorno):

```http
POST http://localhost:9000/oauth2/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic <CLIENTE_Y_SECRETO>

grant_type=client_credentials&scope=read%20write
```

No incluyas secretos ni JWT completos en capturas públicas.

### Consultar movimientos

```http
GET http://localhost:8081/api/cuentas/103/movimientos
Authorization: Bearer <TOKEN>
```

Esta llamada permite verificar la integración de Cuenta Service con
Movimiento Service y la propagación del Bearer Token.

### Publicar una transacción Kafka

```http
POST http://localhost:8083/api/transacciones
Authorization: Bearer <TOKEN>
Content-Type: application/json

{"cuentaId":103,"tipo":"RETIRO","monto":500}
```

Inspecciona el topic `transacciones-bancarias` con Kafka UI y los logs
del consumidor `movimiento-service`. La colección Postman antigua
incluye Basic Auth en algunas solicitudes: utiliza el esquema de
autenticación que exige la configuración final, no el valor antiguo
de la colección.

### Probar resiliencia

1. Ejecuta la consulta de Cuenta Service con Movimiento Service activo.
2. Detén temporalmente el consumidor HTTP:

```cmd
docker compose stop movimiento-service
```

3. Repite la consulta con token válido.
4. Observa la respuesta fallback documentada:

```json
{"mensaje":"Movimiento Service no disponible","movimientos":[]}
```

5. Para probar Retry, revisa evidencia de intentos en logs o métricas.
6. Restaura Movimiento Service:

```cmd
docker compose start movimiento-service
```

### Diagnóstico

```cmd
docker compose logs --tail=100 cuenta-service
docker compose logs --tail=100 movimiento-service
docker compose logs --tail=100 transaccion-service
```

## Evidencias para evaluación

Captura ejecuciones `COMPLETED` y datos H2, respuestas diferenciadas BFF,
JWT y rechazo por permisos, Eureka, Config Server, siete contenedores,
transacción en Postman, Kafka UI y mensaje consumido, además del fallback.
