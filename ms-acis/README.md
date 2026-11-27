# ACIS microservicios

Servicios Spring Boot 3 para la aplicación ACIS. Los cuatro proyectos usan Java 21 y JWT; la clave debe compartirse mediante `JWT_SECRET` (mínimo 32 bytes) en los cuatro procesos. El valor de desarrollo que traen `auth-users`, `ot-gestion`, `terain-photo` y `gateway` no debe usarse en producción.

## Servicios y puertos

| Servicio | Puerto | Rutas |
|---|---:|---|
| `gateway` | 8080 | Entrada de la app; enruta los prefijos de abajo |
| `auth-users` | 8081 | `/api/auth/**`, `/api/usuarios/**` |
| `ot-gestion` | 8082 | `/api/ordenes/**` |
| `terain-photo` | 8083 | `/api/intervenciones/**` |

Ejecuta cada servicio desde su carpeta con `mvnw.cmd spring-boot:run` en Windows o `./mvnw spring-boot:run` en Linux/macOS. Arranca primero los servicios y luego el gateway. Si la app corre en un emulador, configura `AUTH_USERS_URL`, `OT_GESTION_URL` y `TERAIN_PHOTO_URL` con una dirección alcanzable desde ese emulador; `localhost` del emulador no es el computador anfitrión.

## Contratos principales

- `auth-users`: login por `identificador` y `clave`; devuelve `token`, datos de usuario y `rol`. JWT se verifica localmente con la misma `JWT_SECRET`.
- `ot-gestion`: usa `codigoOt` (por ejemplo `OT-1001`) en rutas, con `id` numérico interno. Asigna con `PUT /api/ordenes/{codigoOt}/asignacion` y `{ "tecnicoAsignado": "correo@acis.cl" }`; cambia estados mediante `PATCH /api/ordenes/{codigoOt}/estado` con `{ "estado": "...", "observacionCliente": "..." }`.
- `terain-photo`: check-in JSON `{ "codigoOt": "OT-1001", "ubicacionCheckIn": "-33.4, -70.6" }`; informe JSON `{ "codigoOt": "OT-1001", "diagnostico": "...", "actividad": "...", "materiales": "..." }`; fotografía multipart con campo `file`.

Las bases H2 son locales e independientes para desarrollo. Las fotos se guardan en `terain-photo/uploads/evidencias` por defecto. La persistencia H2 no es una base compartida ni un despliegue de producción.

## Limitaciones conocidas de esta primera integración

`terain-photo` verifica con `ot-gestion` que el técnico autenticado esté asignado y que el estado permita check-in o informe; las llamadas usan el token recibido. La recuperación de contraseña de `auth-users` genera token temporal, pero requiere integrar un canal de entrega verificado (correo/SMS). Ajustar los contratos con el cliente Android antes de la entrega final.
