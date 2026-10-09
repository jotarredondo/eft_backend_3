# EFT Desarrollo Backend III — Banco XYZ

**Asignatura:** Desarrollo Backend III (PBY2203) — Duoc UC
**Proyecto:** Modernización del backend del Banco XYZ
**Repositorio:** https://github.com/jotarredondo/eft_backend_3

## Objetivo

Modernizar componentes de un sistema bancario legacy mediante tres implementaciones
complementarias: procesamiento de datos con Spring Batch, aplicaciones Backend for
Frontend (BFF) para canales web, móvil y ATM, y microservicios con Spring Cloud.

## Organización del repositorio

- `Parte 1/banco_sociedad`: procesos Spring Batch.
- `Parte 2/bank_core_api`: API bancaria común (puerto 8180).
- `Parte 2/web_bff`: canal web (8181).
- `Parte 2/mobile_bff`: canal móvil (8182).
- `Parte 2/atm_bff`: canal ATM (8183).
- `Parte 3/eureka-server`: descubrimiento (8761).
- `Parte 3/config-server`: configuración centralizada (8888).
- `Parte 3/auth_server`: Authorization Server (9000).
- `Parte 3/cuenta-service`: cuentas y consultas de movimientos (8081).
- `Parte 3/movimiento-service`: movimientos y consumidor Kafka (8082).
- `Parte 3/transaccion-service`: productor Kafka (8083).
- `Parte 3/cliente-service`: clientes (8084).
- `Parte 3/config-repo`: configuraciones YAML centralizadas.
- `Parte 3/docker-compose.yml`: despliegue local de los siete servicios Spring.
- `Parte 3/kafka-docker/docker-compose.yml`: infraestructura Kafka en AWS EC2.

Las tres partes se desarrollaron como proyectos diferenciados; no se afirma
que constituyan una sola aplicación integrada en tiempo de ejecución.

## Parte 1: Spring Batch

Se implementaron tres Jobs independientes:

| Job | Propósito |
| --- | --- |
| `dailyTransactionJob` | Validar y persistir transacciones diarias |
| `quarterlyInterestJob` | Calcular intereses usando el dataset trimestral |
| `annualStatementJob` | Consolidar información financiera anual |

Resultados documentados:
- Transacciones diarias: 1.000 leídas, 480 persistidas, 520 filtradas.
- Intereses: 396 registros persistidos, dos duplicados descartados.
- Mejor tiempo de la prueba de concurrencia: 715 ms con tres hilos,
  frente a 868 ms con un hilo.

Incluye validaciones, normalización, retry frente a fallos transitorios y
una política de reejecución para el Job de transacciones.

**Aclaración:** la pauta menciona intereses mensuales, pero el dataset
proporcionado se denomina `intereses_trimestrales.csv`.

## Parte 2: Backend for Frontend

Se implementaron tres aplicaciones BFF, que consumen `bank_core_api`:

| Aplicación | Puerto | Datos adaptados |
| --- | ---: | --- |
| Web BFF | 8181 | Información completa |
| Mobile BFF | 8182 | Respuesta ligera |
| ATM BFF | 8183 | Saldos y retiros |

`bank_core_api` utiliza H2 y proporciona autenticación JWT para el entorno
de pruebas. Los canales exigen scopes `SCOPE_WEB`, `SCOPE_MOBILE` y `SCOPE_ATM`.

## Parte 3: Microservicios Spring Cloud

La solución incorpora cuatro microservicios de negocio y tres servicios
de infraestructura. Eureka permite descubrimiento dinámico; Config Server
centraliza las propiedades; Auth Server proporciona OAuth2 y JWT.

`cuenta-service` consulta a `movimiento-service` mediante REST, Spring Cloud
LoadBalancer y propagación del Bearer JWT.

Se implementó resiliencia con Resilience4j:
- Circuit Breaker.
- Retry configurado con `maxAttempts: 3` y `waitDuration: 1s`.
- Fallback cuando Movimiento Service no está disponible.

**Importante:** para demostrar la cantidad efectiva de reintentos deben
revisarse logs o métricas; ver un fallback no demuestra por sí solo tres intentos.

El flujo asíncrono es:

```text
Postman -> transaccion-service -> Kafka -> movimiento-service -> MySQL
```

Kafka funciona en AWS EC2 con tres brokers, tres nodos ZooKeeper y Kafka UI.
Los siete servicios Spring se ejecutaron localmente con Docker Compose.

## Tecnologías

Java, Spring Boot, Spring Batch, Spring Cloud, Spring Security,
Spring Authorization Server, Spring Data JPA, Resilience4j, Kafka,
Docker, Docker Compose, MySQL, H2, AWS EC2, Maven y Postman.

## Colecciones Postman

- `Parte 2/PARTE2_COLLECTION.postman_collection.json`
- `Parte 3/SPRING_CLOUD.postman_collection.json`

Comprueba las URLs y el método de autenticación después de importarlas;
algunos valores de las colecciones no reflejan la configuración final.

## Documentación adicional

- [Instrucciones de ejecución y prueba](instrucciones.md)
- [Despliegue e infraestructura](despliegue.md)

## Alcance de seguridad

La configuración y usuarios de demostración corresponden a un entorno
académico. Para producción se requerirían HTTPS, administración de secretos,
restricciones de red, observabilidad y automatización CI/CD.