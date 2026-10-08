Parte 2 – Backend for Frontend (BFF)
1. Objetivo

Esta parte de la EFT implementa el patrón Backend for Frontend (BFF) para separar las necesidades de los distintos canales de acceso de un sistema bancario.

La solución dispone de tres BFF independientes:

    Web BFF: entrega información más completa para interfaces web.
    Mobile BFF: entrega una respuesta reducida para dispositivos móviles.
    ATM BFF: entrega información mínima para cajeros automáticos y expone una operación de retiro.

Los tres BFF consumen una API común (bank_core_api) y adaptan la respuesta según las necesidades del canal.
2. Arquitectura general
                         bank_core_api
                            :8180
                  API común + H2 + Auth/JWT
                              |
            +-----------------+-----------------+
            |                 |                 |
            v                 v                 v
       web_bff           mobile_bff          atm_bff
        :8181               :8182              :8183
       SCOPE_WEB         SCOPE_MOBILE        SCOPE_ATM

Cada BFF es una aplicación Spring Boot independiente.
3. Estructura del proyecto
Parte 2/
├── bank_core_api/
├── web_bff/
├── mobile_bff/
├── atm_bff/
└── README.md
bank_core_api

Responsable de:

    Exponer la información base de cuentas.
    Mantener los datos de prueba mediante H2.
    Autenticar usuarios.
    Generar tokens JWT.
    Procesar la operación de retiro utilizada por el ATM BFF.

web_bff

Adapta la respuesta para el canal Web.

Ejemplo de información entregada:
{
  "id": 1,
  "numeroCuenta": 1001,
  "titular": "Ana Torres",
  "tipoCuenta": "AHORRO",
  "saldo": 1250000.00
}
mobile_bff

Entrega una respuesta más liviana para dispositivos móviles.
{
  "numeroCuenta": 1001,
  "tipoCuenta": "AHORRO",
  "saldo": 1250000.00
}
atm_bff

Entrega únicamente los datos necesarios para operaciones de cajero.
{
  "numeroCuenta": 1001,
  "saldoDisponible": 1250000.00
}

También expone una operación de retiro protegida por autorización ATM.
4. Tecnologías utilizadas

    Java
    Spring Boot
    Spring Web
    Spring Security
    OAuth2 Resource Server
    JWT
    Spring Data JPA
    H2 Database
    Maven
    Postman

5. Seguridad

La autenticación se encuentra centralizada en bank_core_api.

Endpoint:
POST http://localhost:8180/auth/login

Usuarios de prueba:
Usuario	Password	Canal
webuser	web123	WEB
mobileuser	mobile123	MOBILE
atmuser	atm123	ATM

Ejemplo:
{
  "username": "webuser",
  "password": "web123"
}

La respuesta contiene un JWT y el canal autorizado.

Cada BFF valida el token y exige un scope específico:
/web/**     -> SCOPE_WEB
/mobile/**  -> SCOPE_MOBILE
/atm/**     -> SCOPE_ATM

Comportamiento esperado:
Prueba	Resultado
Sin token	401 Unauthorized
Token válido para el canal	200 OK
Token válido de otro canal	403 Forbidden
Token inválido	401 Unauthorized

    Las credenciales y la clave JWT utilizadas en este proyecto corresponden únicamente a un entorno académico/local y no deben utilizarse en producción.

6. Puertos
Componente	Puerto
bank_core_api	8180
web_bff	8181
mobile_bff	8182
atm_bff	8183

Los puertos fueron separados de los utilizados en la Parte 3 para permitir ejecutar ambas soluciones simultáneamente durante las pruebas y la presentación.
7. Ejecución

Se recomienda iniciar los componentes en el siguiente orden:

    bank_core_api
    web_bff
    mobile_bff
    atm_bff

Cada proyecto puede ejecutarse desde IntelliJ mediante su clase principal o desde consola con Maven Wrapper.

En Windows:
mvnw.cmd spring-boot:run
8. Endpoints principales
Autenticación
POST http://localhost:8180/auth/login
API base
GET http://localhost:8180/api/accounts
Web BFF
GET http://localhost:8181/web/accounts

Requiere token con:
SCOPE_WEB
Mobile BFF
GET http://localhost:8182/mobile/accounts

Requiere token con:
SCOPE_MOBILE
ATM BFF

Consulta de cuentas/saldo:
GET http://localhost:8183/atm/accounts

Retiro:
POST http://localhost:8183/atm/withdraw

Requiere token con:
SCOPE_ATM

Ejemplo de retiro:
{
  "accountId": 1001,
  "amount": 50000
}

Respuesta esperada:
{
  "accountId": 1001,
  "withdrawnAmount": 50000,
  "remainingBalance": 1200000,
  "status": "APROBADO"
}
9. Pruebas recomendadas

Para demostrar el funcionamiento de la solución:

    Obtener un token WEB y acceder correctamente a /web/accounts.
    Intentar acceder a /mobile/accounts con token WEB y verificar 403 Forbidden.
    Obtener un token MOBILE y acceder correctamente a /mobile/accounts.
    Obtener un token ATM y acceder correctamente a /atm/accounts.
    Ejecutar un retiro con token ATM.
    Ejecutar cualquiera de los endpoints sin token y verificar 401 Unauthorized.

Estas pruebas permiten demostrar autenticación, autorización específica por canal e independencia de los BFF.
10. Resultado

La implementación permite que cada frontend disponga de un backend adaptado a sus necesidades, evitando que todos los canales consuman el mismo contrato de datos.

La solución implementa:

    BFF independiente para Web.
    BFF independiente para Mobile.
    BFF independiente para ATM.
    Respuestas diferenciadas por canal.
    Autenticación centralizada mediante JWT.
    Autorización específica mediante scopes.
    Operación crítica de retiro para ATM.
    Backend común con datos de prueba en H2.

De esta forma se cumple el objetivo de desacoplar los canales frontend del backend común y permitir que cada BFF evolucione de manera independiente.