# auth-users

Microservicio de autenticación y gestión de usuarios ACIS (Spring Boot 3, Java 21, Spring Security, JWT stateless y Spring Data JPA). Ejecuta localmente con H2 en memoria en el puerto `8081`.

## Ejecutar

```powershell
$env:JWT_SECRET = 'una-clave-local-de-al-menos-32-caracteres'
./mvnw.cmd spring-boot:run
```

`JWT_SECRET` debe tener al menos 32 bytes. El valor predeterminado del proyecto sirve solo para desarrollo local y debe reemplazarse en cualquier despliegue. La consola H2 está disponible en `/h2-console` mientras se ejecuta localmente.

## Usuarios de prueba

Las cuentas se insertan en H2 al iniciar si la tabla de usuarios está vacía. Todas las claves se almacenan con BCrypt.

| Rol | Correo | RUT | Clave local |
|---|---|---|---|
| ADMINISTRADOR | admin@acis.cl | 11111111-1 | Admin123! |
| PLANIFICADOR | planificador@acis.cl | 22222222-2 | Planif123! |
| TECNICO | tecnico@acis.cl | 33333333-3 | Tecnico123! |
| CLIENTE | cliente@acis.cl | 44444444-4 | Cliente123! |

También se carga `inactivo@acis.cl` (RUT `55555555-5`, clave `Inactivo123!`) para probar el bloqueo de cuentas inactivas.

## API

Todas las rutas usan JSON. Salvo login, registro y recuperación/restablecimiento, requieren `Authorization: Bearer <JWT>`. El JWT vence según `JWT_EXPIRATION_MS` (por defecto 24 horas).

| Método y ruta | Uso y acceso |
|---|---|
| `POST /api/auth/login` | Público. Recibe `identificador` (correo o RUT) y `clave`; responde JWT, id, identidad, rol y estado, nunca la clave. |
| `POST /api/auth/register` | Público para altas `CLIENTE`; asignar otro rol requiere JWT de administrador. RUT y correo duplicados responden `409`. |
| `GET /api/auth/validate` | Requiere JWT vigente y cuenta activa. |
| `POST /api/auth/recuperar-clave` | Público; acepta `identificador` y responde un mensaje genérico para no revelar si la cuenta existe. |
| `POST /api/auth/restablecer-clave` | Público; recibe `token` de recuperación y `nuevaClave`. Token temporal válido 30 minutos, de un solo uso. |
| `GET /api/usuarios` | ADMINISTRADOR ve todo; PLANIFICADOR puede listar PLANIFICADOR, TECNICO y CLIENTE; TECNICO y CLIENTE reciben `403`. Filtros opcionales `rol` y `activo`. |
| `GET /api/usuarios/perfil` | Usuario autenticado consulta su perfil. |
| `PUT /api/usuarios/perfil` | Usuario modifica nombre/correo propios; para cambiar clave debe enviar clave actual y nueva. |
| `GET /api/usuarios/{id}` | ADMINISTRADOR ve cualquiera; PLANIFICADOR ve técnicos/clientes; cada usuario ve su propio perfil. |
| `PUT /api/usuarios/{id}` | Solo ADMINISTRADOR; edición administrativa. |
| `PATCH /api/usuarios/{id}/estado` | Solo ADMINISTRADOR; activa o desactiva cuentas. |

Respuestas incluyen estados HTTP claros (`400` datos inválidos, `401` autenticación/token no válido, `403` permisos insuficientes, `404` inexistente, `409` duplicados). Roles disponibles: `ADMINISTRADOR`, `PLANIFICADOR`, `TECNICO`, `CLIENTE`.

## Recuperación de clave: integración pendiente

La API genera un token aleatorio con vencimiento de 30 minutos y `restablecer-clave` lo consume una sola vez. Para completar el flujo real debe integrarse un proveedor de notificaciones que entregue ese token al correo verificado del usuario. Por seguridad, el endpoint no retorna el token ni lo registra en logs; hasta integrar el proveedor, la recuperación queda iniciada pero el usuario no recibe el token.

## Permisos

La autorización se valida en el servidor. ADMINISTRADOR administra usuarios y roles; PLANIFICADOR consulta usuarios necesarios para coordinación; TECNICO y CLIENTE acceden a su perfil. El registro público nunca permite autoconcederse roles internos.
