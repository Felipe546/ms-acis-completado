package com.acis.auth_users.controller;

import com.acis.auth_users.dto.ActualizarUsuarioAdminDto;
import com.acis.auth_users.dto.CambiarEstadoDto;
import com.acis.auth_users.dto.MensajeResponseDto;
import com.acis.auth_users.dto.ModificarPerfilDto;
import com.acis.auth_users.model.Rol;
import com.acis.auth_users.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * GET /api/usuarios
     * Lista usuarios según los permisos del rol del usuario autenticado:
     * - ADMINISTRADOR: todos los usuarios (admite filtros opcionales rol y activo)
     * - PLANIFICADOR: usuarios con roles TECNICO, CLIENTE y PLANIFICADOR
     * - TECNICO / CLIENTE: 403 Forbidden
     */
    @GetMapping
    public ResponseEntity<?> listarUsuarios(
            Authentication authentication,
            @RequestParam(required = false) Rol rol,
            @RequestParam(required = false) Boolean activo) {
        try {
            return usuarioService.listarUsuarios(authentication, rol, activo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MensajeResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error interno al listar usuarios: " + e.getMessage()));
        }
    }

    /**
     * GET /api/usuarios/perfil
     * Obtiene los datos del perfil del usuario autenticado actual.
     */
    @GetMapping("/perfil")
    public ResponseEntity<?> obtenerPerfil(Authentication authentication) {
        try {
            return usuarioService.obtenerPerfil(authentication);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MensajeResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error interno al obtener el perfil: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/usuarios/perfil
     * Permite modificar el perfil propio (nombre, email, clave).
     * Requiere verificar la clave actual para cambiar la clave.
     */
    @PutMapping("/perfil")
    public ResponseEntity<?> modificarPerfil(
            Authentication authentication,
            @Valid @RequestBody ModificarPerfilDto dto,
            BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return extraerErroresValidacion(bindingResult);
            }
            return usuarioService.modificarPerfil(authentication, dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MensajeResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error interno al actualizar el perfil: " + e.getMessage()));
        }
    }

    /**
     * GET /api/usuarios/{id}
     * Obtiene el detalle de un usuario específico según permisos.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id, Authentication authentication) {
        try {
            return usuarioService.obtenerPorId(id, authentication);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MensajeResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error interno al obtener el usuario: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/usuarios/{id}
     * Modificación administrativa de usuario (exclusivo ADMINISTRADOR).
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuarioAdmin(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioAdminDto dto,
            BindingResult bindingResult,
            Authentication authentication) {
        try {
            if (bindingResult.hasErrors()) {
                return extraerErroresValidacion(bindingResult);
            }
            return usuarioService.actualizarUsuarioAdmin(id, dto, authentication);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MensajeResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error interno al actualizar el usuario: " + e.getMessage()));
        }
    }

    /**
     * PATCH /api/usuarios/{id}/estado
     * Activación/desactivación de cuenta de usuario (exclusivo ADMINISTRADOR).
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoDto dto,
            BindingResult bindingResult,
            Authentication authentication) {
        try {
            if (bindingResult.hasErrors()) {
                return extraerErroresValidacion(bindingResult);
            }
            return usuarioService.cambiarEstado(id, dto.getActivo(), authentication);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MensajeResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error interno al modificar el estado: " + e.getMessage()));
        }
    }

    private ResponseEntity<?> extraerErroresValidacion(BindingResult bindingResult) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError fe : bindingResult.getFieldErrors()) {
            errores.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(new MensajeResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación en los campos enviados",
                errores));
    }
}
