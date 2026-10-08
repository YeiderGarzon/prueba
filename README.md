# Sistema de gestión de préstamos

Aplicación de demostración con API REST en Spring Boot y cliente Angular. Utiliza autenticación stateless con JWT Bearer y los roles `CUSTOMER` y `ADMIN`. Las solicitudes empiezan como `PENDING`; solo un administrador puede aprobarlas o rechazarlas, una sola vez.

El backend sigue una arquitectura hexagonal con esta estructura:

- `domain/model`: modelos y reglas del dominio, sin dependencias de Spring ni JPA.
- `application/service`: casos de uso de autenticación, usuarios y préstamos; no depende de Spring.
- `application/port/in`: operaciones que la capa web ofrece a los clientes.
- `application/port/out`: contratos que la aplicación necesita para persistencia, autenticación, transacciones y tokens.
- `application/dto`: entradas y salidas de los casos de uso.
- `infrastructure/persistence`: adaptadores Spring Data JPA, entidades relacionales y mapeo hacia el dominio.
- `infrastructure/security`: adaptadores de BCrypt, autenticación Spring Security y emisión/verificación JWT.
- `infrastructure/cache`: caché Ehcache en memoria para consultas de préstamos.
- `infrastructure/web`: controladores REST, validación de payloads y traducción de errores a respuestas HTTP.
- `infrastructure/config`: composición de los casos de uso con sus puertos y adaptadores.

Las dependencias apuntan hacia adentro: los casos de uso usan puertos, y los adaptadores de infraestructura implementan esos puertos. El frontend organiza autenticación y configuración compartida en `core`, y la API y los modelos de préstamos en `features/loans`; `app` contiene la composición y presentación principal.

Las vistas Angular están separadas en componentes y rutas: `/login` para iniciar sesión o registrarse, `/loans` para que el cliente solicite y consulte préstamos, `/profile` para que cualquier usuario autenticado edite su nombre completo, username o contraseña, `/admin/loans` para gestionar préstamos y `/admin/users` para que `ADMIN` edite cuentas y roles. Las rutas aplican guards de acuerdo con el rol autenticado.

## Requisitos

- JDK 17 o posterior
- Node.js y npm

## Configuración y ejecución

En PowerShell, configura la clave JWT y la contraseña de la base de datos de Supabase. En el panel de Supabase, obtén la contraseña en **Project Settings → Database**; no uses la URL del proyecto como contraseña. Luego inicia el backend desde esa misma terminal:

```powershell
$jwtBytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($jwtBytes)
$rng.Dispose()
$env:APP_JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
$env:SUPABASE_DB_PASSWORD = Read-Host "Contraseña de la base de datos Supabase"
cd prueba-back
.\mvnw.cmd spring-boot:run
```

La conexión predeterminada usa el Session pooler de Supabase en `aws-0-us-east-1.pooler.supabase.com:5432`, base `postgres`, usuario `postgres.yulrthrlmezbyvvrgeuh` y TLS obligatorio. La contraseña de la base debe definirse en `SUPABASE_DB_PASSWORD`. `SUPABASE_JDBC_URL` y `SUPABASE_DB_USER` permiten reemplazar la URL y el usuario; si los datos del pooler cambian, copia el host, puerto, nombre de usuario y base de **Connect → Session pooler** y usa el formato `jdbc:postgresql://<host>:<puerto>/postgres?sslmode=require`, junto con el usuario completo que muestra Supabase.

Si ya tienes una base con préstamos creados por la versión anterior, antes de volver a iniciar la aplicación ejecuta una sola vez el script [V2__loans_reference_applicant_id.sql](./prueba-back/src/main/resources/db/manual/V2__loans_reference_applicant_id.sql) desde Supabase SQL Editor. El script relaciona las filas existentes por nombre de usuario, exige que todos los préstamos puedan asociarse y luego deja `applicant_id` como clave foránea obligatoria. Haz un respaldo de la base antes de aplicar la migración; si informa préstamos sin usuario, corrige la relación de esos datos antes de repetirla.

Para agregar el nombre completo a cuentas existentes, ejecuta también una sola vez [V3__add_user_full_name.sql](./prueba-back/src/main/resources/db/manual/V3__add_user_full_name.sql) en Supabase SQL Editor. La migración inicializa las cuentas actuales con el username como nombre completo; cada usuario o administrador podrá cambiarlo después desde la aplicación.

Hibernate tiene configurado explícitamente el dialecto PostgreSQL. Si el arranque sigue fallando, revisa la primera excepción de conexión JDBC antes de `Unable to determine Dialect`: normalmente indica host/puerto inaccesible, contraseña de base incorrecta o usuario de pooler incompleto. El dialecto no sustituye la conexión y no se debe ocultar un error de autenticación o red.

Conserva la misma clave `APP_JWT_SECRET` entre reinicios. En Eclipse, define `APP_JWT_SECRET` y `SUPABASE_DB_PASSWORD` en **Run Configurations → Environment**. No escribas contraseñas ni secretos en el repositorio.

## Crear un administrador

Registra primero la cuenta desde la pantalla de registro, usando una contraseña de al menos 12 caracteres. El registro público siempre asigna el rol `CUSTOMER`. Después, en **Supabase → SQL Editor**, asigna el rol administrativo a esa cuenta:

```sql
UPDATE public.app_users
SET role = 'ADMIN'
WHERE username = 'admin'
RETURNING id, username, role;
```

Usa en la cláusula `WHERE` el nombre exacto con el que registraste la cuenta y verifica que se haya actualizado una sola fila. Los roles viven en la base de datos y se leen al iniciar sesión; cierra sesión y vuelve a entrar para obtener un token con el rol actualizado. No agregues un rol al JSON de registro: nunca se acepta desde el cliente.

En otra terminal inicia el cliente:

```powershell
cd prueba-front\prueba
npm.cmd install
npm.cmd start
```

Abre `http://localhost:4200`. Las cuentas y solicitudes se guardan en PostgreSQL en tu proyecto Supabase. El cliente conserva la sesión en el navegador durante un máximo de 10 minutos desde el inicio de sesión para que sobreviva a una recarga o reinicio; al vencer ese plazo o cerrar sesión, elimina el token guardado. El token JWT del backend expira a los 30 minutos, pero el cliente deja de usarlo a los 10 minutos.

## Autenticación y autorización

| Método | Ruta | Acceso | Descripción |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Público | Registrar un cliente; el rol siempre es `CUSTOMER` |
| `POST` | `/api/auth/login` | Público | Validar credenciales y obtener un JWT |
| `GET` | `/api/auth/me` | Autenticado | Consultar usuario y rol del token |
| `POST` | `/api/loans` | `CUSTOMER` | Crear una solicitud; el solicitante se toma del token |
| `GET` | `/api/loans` | Autenticado | Un cliente ve solo sus solicitudes; ADMIN ve todas |
| `GET` | `/api/loans/{id}` | Autenticado | Consultar una solicitud propia; ADMIN puede consultar cualquiera |
| `PUT` | `/api/loans/{id}` | Propietario o ADMIN | Actualizar monto y plazo mientras esté `PENDING` |
| `DELETE` | `/api/loans/{id}` | Propietario o ADMIN | Eliminar mientras esté `PENDING` |
| `PATCH` | `/api/loans/{id}/decision` | `ADMIN` | Aprobar o rechazar (`status: APPROVED` o `REJECTED`) |
| `GET` | `/api/users` | `ADMIN` | Listar usuarios sin exponer hashes de contraseña |
| `GET` | `/api/users/{id}` | `ADMIN` | Consultar un usuario |
| `POST` | `/api/users` | `ADMIN` | Crear usuario y asignar rol explícitamente |
| `PUT` | `/api/users/{id}` | `ADMIN` | Actualizar username, nombre completo, rol y opcionalmente contraseña |
| `DELETE` | `/api/users/{id}` | `ADMIN` | Eliminar usuario sin préstamos asociados |
| `GET` | `/api/users/me` | Autenticado | Consultar el perfil propio |
| `PUT` | `/api/users/me` | Autenticado | Cambiar username, nombre completo y opcionalmente contraseña; no modifica el rol |

Incluye el token devuelto por `/api/auth/login` en las rutas protegidas:

```http
Authorization: Bearer <accessToken>
```

El registro de cuentas requiere nombre completo (hasta 120 caracteres), username de 3 a 40 caracteres (`a-z`, `A-Z`, números, `.`, `_` o `-`) y contraseña de al menos 12 caracteres. El nombre completo es independiente del username, que se usa para iniciar sesión. Las contraseñas se guardan con BCrypt. El JWT expira a los 30 minutos. Los clientes no pueden elegir su rol en el registro ni acceder a solicitudes ajenas; la API verifica estas reglas aunque se evite la interfaz Angular.

El monto mínimo es 100 y el plazo debe ser positivo. Las solicitudes solo pueden editarse o eliminarse mientras estén pendientes; una segunda decisión sobre un préstamo resuelto devuelve `409 Conflict`. Cada préstamo se relaciona con la cuenta mediante `applicant_id`, no por nombre de usuario; la API devuelve por separado username y nombre completo del solicitante. Cambiar el username o el nombre completo no rompe ni reasigna la relación. El registro público siempre crea clientes; solo `ADMIN` puede gestionar usuarios. Las contraseñas nuevas se guardan con BCrypt. No se permite eliminar el usuario autenticado, el último administrador ni usuarios con préstamos asociados.

Los errores REST incluyen `status`, `error`, `message` y `detail`; las entradas inválidas agregan `fieldErrors` por campo. La API responde con `400` para validaciones y JSON mal formado, `401` para falta de autenticación, `403` para permisos insuficientes, `404` para recursos inexistentes, `409` para conflictos de negocio o integridad, `405` para métodos no admitidos y `500` para fallos inesperados. Los errores internos se registran en el backend sin exponer detalles técnicos al cliente.

Las lecturas de préstamos por ID, lista global y lista por solicitante usan Ehcache local con TTL de 20 segundos y límite de 500 entradas por región. Crear, editar, decidir o eliminar préstamos invalida las regiones; también se invalida la caché al modificar un nombre de usuario o nombre completo, y se vuelve a limpiar después del commit para evitar servir el estado previo. Por ser caché en memoria de una instancia, no se comparte entre varias réplicas: en despliegues escalados usa una caché distribuida o invalidación entre instancias.

La aplicación mantiene Spring MVC como servidor HTTP y usa WebFlux (`Mono`) únicamente en `POST /api/loans`. Como JPA/JDBC son bloqueantes, esa operación se ejecuta en `Schedulers.boundedElastic()` para no ocupar el hilo de la petición MVC mientras espera a la base de datos. Esto ayuda a contener la espera concurrente, pero no hace no bloqueante a JPA ni aumenta por sí solo la capacidad del pool/conexión de PostgreSQL; para una carga alta, mide primero y ajusta el pool JDBC y los límites del servicio. Spring Data JPA realiza el acceso a PostgreSQL y Hibernate Validator valida los DTO de entrada.

## Pruebas

```powershell
cd prueba-back
.\mvnw.cmd test
cd ..\prueba-front\prueba
npm.cmd test -- --watch=false
```

Esta aplicación es una simulación educativa, no un sistema bancario listo para producción. Antes de desplegarla se requieren, entre otros controles, HTTPS, gestión segura de secretos, protección contra fuerza bruta, políticas de contraseñas y revisión de seguridad.

Credencial de admin de prueba:
Username: admin
password: adminadminadmin
