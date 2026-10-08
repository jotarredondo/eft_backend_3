Banco BFF - Desarrollo Backend III

Proyecto formativo correspondiente a la implementación del patrón arquitectónico Backend for Frontend (BFF) para un sistema bancario.

Objetivo

Implementar una arquitectura BFF que permita entregar respuestas personalizadas según las necesidades de distintos tipos de clientes.

El sistema utiliza una API principal conectada a MySQL y distintos BFF que consumen esa API y adaptan la información para cada canal.

Arquitectura

MySQL
↓
bank-core-api :8080
↓

↓ ↓ ↓
bff-web :8081 bff-mobile :8082 bff-atm :8083

Proyectos

bank-core-api

API principal encargada de acceder a los datos almacenados en MySQL.

Endpoint principal:

GET http://localhost:8080/api/accounts

Entrega la información completa de las cuentas bancarias.

bff-web

Backend específico para aplicaciones web.

Consume bank-core-api y entrega una respuesta orientada a interfaces web, incluyendo información más completa de las cuentas.

Endpoint:

GET http://localhost:8081/web/accounts

Ejemplo de respuesta:

{
"id": 1,
"numeroCuenta": 1001,
"titular": "Ana Torres",
"tipoCuenta": "AHORRO",
"saldo": 1250000
}

bff-mobile

Backend específico para aplicaciones móviles.

Consume la misma API principal, pero entrega una respuesta más liviana con solo la información necesaria para reducir el volumen de datos transferidos.

Endpoint:

GET http://localhost:8082/mobile/accounts

Ejemplo de respuesta:

{
"numeroCuenta": 1001,
"tipoCuenta": "AHORRO",
"saldo": 1250000
}

bff-atm

Backend destinado al canal de cajeros automáticos.

Su objetivo es exponer operaciones específicas y simplificadas para el canal ATM, como consulta de saldo y operaciones bancarias críticas.

Endpoint:

GET http://localhost:8083/atm/accounts

Ejemplo de respuestas:
{
    "numeroCuenta": 1001,
    "saldoDisponible": 1250000.00
}

Tecnologías utilizadas

Java 21
Spring Boot
Spring Web
Spring Data JPA
MySQL
Maven
Lombok
RestClient
Postman

Base de datos

Base utilizada:

banco_bff

La conexión se configura en:

bank-core-api/src/main/resources/application.properties

Ejemplo:

spring.datasource.url=jdbc:mysql://localhost:3306/banco_bff?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=create
spring.jpa.show-sql=true

Ejecución

Los proyectos deben ejecutarse de forma independiente.

Orden recomendado:

Iniciar MySQL.
Ejecutar bank-core-api.
Ejecutar bff-web.
Ejecutar bff-mobile.
Ejecutar bff-atm, si se desea probar este canal.

Puertos utilizados:

bank-core-api -> 8080
bff-web -> 8081
bff-mobile -> 8082
bff-atm -> 8083

Pruebas

Las APIs pueden probarse utilizando Postman.

El repositorio incluye una colección de Postman con las solicitudes utilizadas durante el desarrollo.

Patrón Backend for Frontend

La implementación permite que distintos clientes consuman una misma fuente de datos, pero reciban respuestas adaptadas a sus necesidades.

Web recibe información más completa.

Mobile recibe una respuesta más liviana.

ATM dispone de una interfaz especializada para operaciones bancarias.

Esto evita que cada frontend tenga que realizar transformaciones innecesarias y permite personalizar la comunicación según el canal.
