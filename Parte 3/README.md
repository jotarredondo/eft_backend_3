Desarrollando microservicios y resiliencia en la nube con Spring Cloud

## Descripción

Proyecto desarrollado para la actividad sumativa de Semana 8 de Desarrollo Backend III.

La solución implementa una arquitectura de microservicios utilizando Spring Cloud, OAuth2.0, Resilience4j, Docker, Docker Compose y mensajería asíncrona con Apache Kafka.

## Arquitectura

La solución está compuesta por los siguientes servicios:

- Eureka Server
- Config Server
- Authorization Server
- Cuenta Service
- Movimiento Service
- Transacción Service

### Puertos

| Servicio | Puerto |
|---|---:|
| Eureka Server | 8761 |
| Config Server | 8888 |
| Auth Server | 9000 |
| Cuenta Service | 8081 |
| Movimiento Service | 8082 |
| Transacción Service | 8083 |

## Seguridad OAuth2.0

Se implementó un Authorization Server que genera tokens JWT mediante OAuth2.0.

El microservicio `cuenta-service` funciona como Resource Server y protege sus endpoints mediante Bearer Token.

### Obtención de token

POST http://localhost:9000/oauth2/token

Basic Auth:

- Client ID: banco-client
- Client Secret: banco-secret

Body x-www-form-urlencoded:

- grant_type=client_credentials
- scope=read write

### Endpoint protegido

GET http://localhost:8081/api/cuentas/103/movimientos

Sin token válido, el servicio responde 401 Unauthorized.

Con Bearer Token válido, el servicio permite acceder al recurso protegido.

## Resilience4j

`cuenta-service` consume `movimiento-service`.

Se implementó Circuit Breaker con Resilience4j para manejar la indisponibilidad del servicio de movimientos.

Cuando `movimiento-service` no está disponible, el sistema responde mediante un fallback:

```json
{
  "mensaje": "Movimiento Service no disponible",
  "movimientos": []
}
```

## Mensajería asíncrona con Kafka

La arquitectura utiliza Kafka para la comunicación entre:

transaccion-service -> Kafka -> movimiento-service

Topic utilizado:

`transacciones-bancarias`

Configuración del topic:

- Particiones: 3
- Factor de replicación: 2
- In Sync Replicas: 6 de 6
- URP: 0

Kafka se ejecuta en una instancia AWS EC2 mediante Docker Compose.

`transaccion-service` publica eventos de transacciones y `movimiento-service` los consume de forma asíncrona.

### Ejemplo de transacción

POST http://localhost:8083/api/transacciones

```json
{
  "cuentaId": 103,
  "tipo": "RETIRO",
  "monto": 500
}
```

## Docker

Cada servicio posee su propio Dockerfile.

Imágenes utilizadas:

- eureka-server:1.0
- config-server:1.0
- auth-server:1.0
- cuenta-service:1.0
- movimiento-service:1.0
- transaccion-service:1.0

## Docker Compose

Los servicios Spring se orquestan mediante Docker Compose.

Para iniciar:

```bash
docker compose up -d
```

Para verificar:

```bash
docker compose ps
```

Para detener:

```bash
docker compose down
```

## Kafka en AWS EC2

Kafka se ejecuta en una instancia EC2 utilizando Docker Compose.

Componentes:

- 3 brokers Kafka
- 3 nodos ZooKeeper
- Kafka UI

Puertos externos:

- 29092
- 39092
- 49092
- 8090

El cluster utiliza una Elastic IP para permitir la conexión desde los microservicios ejecutados localmente en Docker.

## Configuración centralizada

Los microservicios consumen sus configuraciones desde Spring Cloud Config Server.

Los archivos de configuración se almacenan en:

`config-repo/`

Entre las propiedades centralizadas se encuentran:

- puertos
- datasource
- Eureka
- Kafka
- OAuth2 Resource Server
- Resilience4j

## Ejecución de la solución

1. Iniciar la instancia EC2.
2. Levantar Kafka con Docker Compose en EC2.
3. Verificar los brokers en Kafka UI.
4. Crear el topic `transacciones-bancarias`.
5. Levantar los microservicios localmente mediante Docker Compose.
6. Verificar el registro de servicios en Eureka.
7. Obtener un token OAuth2.
8. Probar el endpoint protegido de `cuenta-service`.
9. Enviar una transacción desde `transaccion-service`.
10. Verificar el mensaje en Kafka UI.
11. Verificar el consumo del evento en `movimiento-service`.
12. Detener `movimiento-service` para probar el Circuit Breaker.
13. Verificar la respuesta fallback desde `cuenta-service`.

## Evidencias

Se incluyen capturas de:

- Generación de token OAuth2
- Respuesta 401 sin token
- Respuesta exitosa con Bearer Token
- Imágenes Docker
- Docker Compose con servicios activos
- Servicios registrados en Eureka
- Kafka ejecutándose en EC2
- Kafka UI con 3 brokers
- Topic `transacciones-bancarias`
- Mensaje publicado en Kafka
- Evento consumido por `movimiento-service`
- Circuit Breaker y respuesta fallback

## Tecnologías utilizadas

- Java 21
- Spring Boot
- Spring Cloud
- Spring Security
- OAuth2
- JWT
- Resilience4j
- Apache Kafka
- ZooKeeper
- Docker
- Docker Compose
- MySQL
- AWS EC2
- Maven

## Autor

Jose Arredondo