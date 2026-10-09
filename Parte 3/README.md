# Parte 3 — Microservicios resilientes con Spring Cloud

**Proyecto:** EFT Desarrollo Backend III · Banco XYZ · Duoc UC  
**Repositorio:** [eft_backend_3](https://github.com/jotarredondo/eft_backend_3)

## Objetivo y alcance

Implementar una arquitectura de microservicios de negocio con descubrimiento de servicios, configuración centralizada, seguridad OAuth2/JWT, tolerancia a fallos y mensajería asíncrona. Los siete servicios Spring se ejecutan localmente mediante Docker Compose. La infraestructura Kafka se despliega por separado en una instancia AWS EC2; no se afirma que todos los microservicios Spring estén ejecutándose en AWS.

## Componentes

| Componente | Puerto local | Función |
| --- | ---: | --- |
| `eureka-server` | 8761 | Registro y descubrimiento de servicios |
| `config-server` | 8888 | Configuración YAML centralizada (`config-repo/`) |
| `auth-server` (carpeta `auth_server/`) | 9000 | OAuth2 / emisión de JWT |
| `cuenta-service` | 8081 | Consultas de cuentas e integración con movimientos |
| `movimiento-service` | 8082 | Persistencia MySQL y consumo de eventos Kafka |
| `transaccion-service` | 8083 | Recepción y publicación de transacciones Kafka |
| `cliente-service` | 8084 | Gestión de clientes en MySQL |

### Integraciones principales

```text
Postman -> cuenta-service -> [REST + Bearer JWT + LoadBalancer] -> movimiento-service -> MySQL
Postman -> transaccion-service -> [topic: transacciones-bancarias] -> Kafka EC2 -> movimiento-service -> MySQL
                                | 
                     Eureka / Config Server / Auth Server
```

## Requisitos previos

- Java y Maven/Maven Wrapper compatibles con los módulos.
- Docker Desktop y Docker Compose.
- MySQL accesible en el equipo anfitrión por TCP/3306.
- Conexión autorizada a los brokers Kafka desplegados en EC2.
- Postman para importar `SPRING_CLOUD.postman_collection.json`.

## 1. Preparar MySQL

El script [database/init_banco_bff.sql](database/init_banco_bff.sql) crea la
base `banco_bff` y las tablas necesarias **sin ejecutar DROP ni DELETE**.
Incluye `cliente` y `movimiento_anual`, ajustadas a las entidades JPA de
`cliente-service` y `movimiento-service`, además de las tablas complementarias
que contiene el script de trabajo proporcionado.

Desde MySQL Workbench puedes abrir y ejecutar `database/init_banco_bff.sql`.
Alternativamente, desde la carpeta `Parte 3`:

```bash
mysql -u <USUARIO_MYSQL> -p < database/init_banco_bff.sql
```

### Carga opcional de datos de prueba

El archivo [database/datos_ejemplo.sql](database/datos_ejemplo.sql) conserva
los registros de muestra aportados para `cuenta_interes`,
`transaccion_diaria`, `cliente`, `account` y `bank_transaction`.

En `movimiento_anual` solo importa datos cuyos valores sean compatibles
con el enum Java actual (`PAGO`, `TRANSFERENCIA`, `DEPOSITO`): los registros
históricos `COMPRA` y `RETIRO` se omiten expresamente en esa tabla.

**Ejecuta `datos_ejemplo.sql` solo una vez, sobre una base vacía.**
No lo ejecutes sobre tu base actual con registros porque puede duplicarlos.

```bash
mysql -u <USUARIO_MYSQL> -p < database/datos_ejemplo.sql
```

La tabla `movimiento_anual` del script anterior tenía la columna
`transaccion`; sin embargo, la entidad actual `MovimientoAnual.java`
usa `tipo`. El nuevo script define `tipo` directamente.
Si ya tienes la tabla creada con otro esquema, `CREATE TABLE IF NOT EXISTS`
no la migrará: revisa primero `DESCRIBE movimiento_anual;` y planifica
la migración conservando los datos existentes.

La configuración de `movimiento-service` habilita
`spring.jpa.hibernate.ddl-auto: update`; en producción sería preferible
un sistema explícito de migraciones.

**Seguridad:** los YAML actualmente versionados contienen credenciales
MySQL. Para entornos compartidos, cambia las credenciales y utiliza
variables de entorno o gestión de secretos.

## 2. Construir imágenes cuando cambie el código

El `docker-compose.yml` define imágenes `:1.0` **sin bloques `build:`**. Por ello, si se modifica Java, hay que compilar y construir la imagen del módulo correspondiente. Desde la carpeta `Parte 3`, por ejemplo:

```cmd
cd cuenta-service
mvnw.cmd clean package -DskipTests
docker build -t cuenta-service:1.0 .
cd ..
```

Repite para cada servicio modificado, usando el nombre de imagen que aparece en `docker-compose.yml`. Si no modificaste código Java ni Dockerfiles y las imágenes ya existen, este paso no es necesario.

## 3. Levantar los servicios

Detén primero las ejecuciones locales de IntelliJ que puedan ocupar los mismos puertos. Desde `Parte 3`:

```cmd
docker compose up -d
docker compose ps
```

Comprueba Eureka en http://localhost:8761. Los microservicios utilizan el hostname `eureka-server` **dentro de Docker**. En `transaccion-service`, `SPRING_APPLICATION_JSON` fija explícitamente `eureka.client.serviceUrl.defaultZone` para el registro en Eureka.

Config Server puede verificarse con:

```http
GET http://localhost:8888/cuenta-service/default
```

Los archivos en `config-repo/` están montados en Config Server. Después de cambios de configuración puede ser necesario reiniciar el servicio cliente.

## 4. Seguridad OAuth2 y JWT

El servidor de autorización expone:

```http
POST http://localhost:9000/oauth2/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic <CLIENT_ID_Y_CLIENT_SECRET>

grant_type=client_credentials&scope=read%20write
```

Obtén un `access_token` y úsalo como `Authorization: Bearer <TOKEN>` en los endpoints protegidos. No publiques tokens o secretos completos en capturas.

## 5. Probar comunicación síncrona

```http
GET http://localhost:8081/api/cuentas/103/movimientos
Authorization: Bearer <TOKEN>
```

`cuenta-service` invoca a `MOVIMIENTO-SERVICE` mediante `RestTemplate` con `@LoadBalanced` y propaga el JWT recibido. Este flujo permite demostrar descubrimiento dinámico y seguridad en llamadas internas.

## 6. Resiliencia: Circuit Breaker, Retry y fallback

`MovimientoClient` protege la llamada HTTP a `movimiento-service` mediante Resilience4j. En `config-repo/cuenta-service.yml` se configura:

- Retry: tres intentos totales (`maxAttempts: 3`) con espera de un segundo.
- Circuit Breaker: ventana de diez llamadas, umbral de fallos del 50 % y espera de diez segundos en estado abierto.
- Fallback: respuesta alternativa cuando no se consiguen movimientos.

Ejemplo documentado de fallback:

```json
{"mensaje":"Movimiento Service no disponible","movimientos":[]}
```

Prueba controlada: consulta el endpoint, ejecuta `docker compose stop movimiento-service`, vuelve a consultar y posteriormente recupera el servicio con `docker compose start movimiento-service`. Para demostrar los reintentos, revisa logs o métricas: la mera aparición del fallback no prueba cuántos intentos ocurrieron.

## 7. Kafka en AWS EC2

Infraestructura definida en `kafka-docker/docker-compose.yml`:

- Tres brokers Kafka (`29092`, `39092`, `49092` expuestos externamente).
- Tres nodos ZooKeeper.
- Kafka UI publicado en el **puerto externo 8090**.

El topic de negocio es `transacciones-bancarias`. `transaccion-service` publica y `movimiento-service` consume.

Prueba de publicación:

```http
POST http://localhost:8083/api/transacciones
Authorization: Bearer <TOKEN>
Content-Type: application/json

{"cuentaId":103,"tipo":"RETIRO","monto":500}
```

Inspecciona el topic en Kafka UI usando `http://<IP_EC2>:8090` y los registros de `movimiento-service`. Para establecer una infraestructura desde cero, revisa las instrucciones detalladas en el `despliegue.md` del repositorio principal.

**Nota técnica:** la solicitud puede usar `RETIRO` como evento de negocio, pero la enumeración JPA `TipoMovimiento` actualmente solo contiene `PAGO`, `TRANSFERENCIA` y `DEPOSITO`. No asumas que todo evento `RETIRO` se persiste sin verificar el mapeo en el consumidor.

## 8. Base de datos y verificación

```sql
USE banco_bff;
SHOW TABLES;
SELECT * FROM cliente;
SELECT * FROM movimiento_anual ORDER BY id DESC LIMIT 20;
```

Verifica datos reales en las tablas y los logs del servicio consumidor. Un HTTP exitoso del productor por sí solo no garantiza que el mensaje se haya persistido.

## 9. Comandos de diagnóstico

```cmd
docker compose ps
docker compose logs --tail=100 cuenta-service
docker compose logs --tail=100 movimiento-service
docker compose logs --tail=100 transaccion-service
```

Para detener los servicios Spring sin eliminar datos persistidos en volúmenes:

```cmd
docker compose down
```

No utilices `docker compose down -v` en entornos con volúmenes que quieras conservar.

## Referencias del proyecto

- [Colección Postman de la Parte 3](SPRING_CLOUD.postman_collection.json)
- [Configuraciones Spring Cloud](config-repo/)
- [Docker Compose Spring](docker-compose.yml)
- [Docker Compose Kafka EC2](kafka-docker/docker-compose.yml)
- [Script SQL MySQL](database/init_banco_bff.sql)
