# Despliegue de la solución en entorno local y AWS EC2

## Arquitectura del despliegue

El entorno de evaluación utiliza infraestructura híbrida:

- **Docker local:** Eureka, Config Server, Auth Server y cuatro
  microservicios Spring.
- **AWS EC2:** tres brokers Kafka, tres nodos ZooKeeper y Kafka UI.
- **MySQL local:** conectado a algunos microservicios mediante
  `host.docker.internal`.

No se afirma que los siete microservicios Spring estén alojados en EC2.

## 1. Requisitos

- Docker Desktop/Engine y Docker Compose.
- JDK y Maven Wrapper para construir los JAR.
- MySQL disponible y base `banco_bff` configurada.
- Acceso SSH a una instancia EC2 con Docker instalado.
- Security Group con reglas restringidas.
- Puertos libres en el equipo local.

## 2. Construir imágenes Docker

El `docker-compose.yml` de `Parte 3` utiliza imágenes con etiqueta `:1.0`
sin bloques `build:`. Por eso `docker compose up --build` no recompila
por sí solo el código Java.

Ejemplo desde CMD, situado en `Parte 3`:

```cmd
cd cuenta-service
mvnw.cmd clean package -DskipTests
docker build -t cuenta-service:1.0 .
cd ..
```

Repite el procedimiento por cada proyecto cuyo código haya cambiado,
respetando los nombres de imagen:

```text
eureka-server:1.0
config-server:1.0
auth-server:1.0
cuenta-service:1.0
movimiento-service:1.0
transaccion-service:1.0
cliente-service:1.0
```

La carpeta del Authorization Server en el repositorio se denomina
`auth_server`, mientras que la imagen Docker es `auth-server:1.0`.

**Nota:** `-DskipTests` no ejecuta las pruebas; usa
`mvnw.cmd test` por separado para validar cada módulo.

## 3. Levantar los siete servicios Spring

Desde `Parte 3`:

```cmd
docker compose up -d
docker compose ps
```

Servicios expuestos desde el anfitrión:

| Componente | URL |
| --- | --- |
| Eureka | http://localhost:8761 |
| Config Server | http://localhost:8888 |
| Auth Server | http://localhost:9000 |
| Cuenta | http://localhost:8081 |
| Movimiento | http://localhost:8082 |
| Transacción | http://localhost:8083 |
| Cliente | http://localhost:8084 |

**Red Docker:** entre contenedores, utiliza direcciones como
`http://eureka-server:8761/eureka/`, no `localhost:8761`.

La configuración versionada de Transaccion Service incluye
`SPRING_APPLICATION_JSON` para fijar explícitamente
`eureka.client.serviceUrl.defaultZone` en la red Docker.

## 4. Preparar Kafka en EC2

El archivo `Parte 3/kafka-docker/docker-compose.yml` declara:

- `zookeeper-1`, `zookeeper-2`, `zookeeper-3`.
- `kafka-1`, `kafka-2`, `kafka-3`.
- `kafka-ui`.

Copia el Compose a la instancia EC2 y ejecútalo allí:

```bash
sudo docker compose up -d
sudo docker compose ps
```

Si el clúster ya existe, comprueba sus contenedores antes de crear
recursos nuevos. Nunca utilices `docker compose down -v` si deseas
preservar los volúmenes de Kafka.

Puertos publicados del Compose:

```text
Kafka broker 1: 29092
Kafka broker 2: 39092
Kafka broker 3: 49092
Kafka UI:       8090 (contenedor interno 8080)
```

**Importante:** el archivo de ejemplo incorpora la IP pública de la
instancia utilizada en la prueba: `52.204.246.248`. Si se despliega
en otra EC2, actualiza `KAFKA_ADVERTISED_LISTENERS` y
`spring.kafka.bootstrap-servers` usando su dirección real.

### Verificación del clúster

```bash
sudo docker ps
sudo docker port kafka-ui
sudo docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --list
```

Para una instalación nueva, únicamente si el topic no existe:

```bash
sudo docker exec kafka-1 kafka-topics \
  --bootstrap-server localhost:9092 \
  --create --topic transacciones-bancarias \
  --partitions 3 --replication-factor 2
```

Consulta sus características efectivas:

```bash
sudo docker exec kafka-1 kafka-topics \
  --bootstrap-server localhost:9092 \
  --describe --topic transacciones-bancarias
```

El archivo declara `KAFKA_AUTO_CREATE_TOPICS_ENABLE=false`,
por lo que en un clúster recién creado se necesita crear el topic.

### Security Group

Permite únicamente el acceso imprescindible:

| Puerto | Uso | Origen recomendado |
| --- | --- | --- |
| 22 | SSH | IP de administración |
| 29092, 39092, 49092 | Clientes Kafka | IP autorizada |
| 8090 | Kafka UI | IP del evaluador/administrador |

Evita exponer estos puertos de forma abierta a Internet.
El ejemplo usa Kafka `PLAINTEXT` y no incluye autenticación ni TLS;
se trata de una configuración de demostración, no de producción.

Kafka UI desde el navegador:

```text
http://<IP_O_DNS_DE_LA_EC2>:8090
```

## 5. Conexión MySQL

El Compose local utiliza, entre otras propiedades:

```text
jdbc:mysql://host.docker.internal:3306/banco_bff?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

Verifica que MySQL acepte conexiones desde Docker, que la base exista
y que las credenciales estén configuradas fuera de archivos públicos.

## 6. Verificación integral

1. `docker compose ps`: siete servicios Spring activos.
2. Eureka: servicios de negocio registrados.
3. Config Server: respuesta de `/cuenta-service/default`.
4. Auth Server: token OAuth2 válido.
5. Cuenta Service: consulta protegida de movimientos.
6. Transaccion Service: envío de evento.
7. Kafka UI: topic `transacciones-bancarias` y mensajes.
8. Movimiento Service: consumo del evento.
9. Fallback de Cuenta Service: respuesta controlada si Movimiento
   Service se detiene temporalmente.

## 7. Mantenimiento y errores frecuentes

**Puerto ocupado:** detén la aplicación local que ocupa el puerto
antes de levantar Docker. No finalices procesos desconocidos.

**Eureka usa localhost dentro de Docker:** revisa `defaultZone`
y la dirección `http://eureka-server:8761/eureka/`.

**Kafka no responde:** revisa EC2, dirección pública, reglas del
Security Group y estado de ZooKeeper y brokers.

**Kafka UI no abre en 8080:** el puerto exterior correcto es `8090`.

**Cambios Java no aparecen:** compila el módulo, reconstruye su imagen
y recrea solo su contenedor:

```cmd
docker compose up -d --no-deps --force-recreate cuenta-service
```

**Cambio YAML:** los archivos `config-repo` están montados en
Config Server; verifica cómo el servicio cliente vuelve a cargar
sus propiedades. Reiniciar el cliente puede ser necesario.

## 8. Próximas mejoras

HTTPS/TLS, gestión de secretos, Kafka seguro, monitoreo centralizado,
trazas distribuidas, pruebas de integración, CI/CD, backups y despliegue
de todos los microservicios en infraestructura AWS.
