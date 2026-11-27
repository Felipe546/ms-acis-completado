package com.acis.auth_users.controller;

import com.acis.auth_users.dto.LoginRequestDto;
import com.acis.auth_users.dto.MensajeResponseDto;
import com.acis.auth_users.dto.RecuperarClaveDto;
import com.acis.auth_users.dto.RegistroUsuarioDto;
import com.acis.auth_users.dto.RestablecerClaveDto;
import com.acis.auth_users.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login
     * Autentica con el identificador (correo o RUT) y clave.
     * Retorna token JWT, identidad y rol.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto dto, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return extraerErroresValidacion(bindingResult);
            }
            return authService.login(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MensajeResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error interno al procesar el inicio de sesión: " + e.getMessage()));
        }
    }

    /**
     * POST /api/auth/register
     * Registra un nuevo usuario con validación y clave cifrada.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistroUsuarioDto dto,
                                      BindingResult bindingResult,
                                      Authentication authentication) {
        try {
            if (bindingResult.hasErrors()) {
                return extraerErroresValidacion(bindingResult);
            }
            return authService.register(dto, authentication);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MensajeResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error interno al procesar el registro: " + e.getMessage()));
        }
    }

    /**
     * GET /api/auth/validate
     * Valida el token JWT en el encabezado Authorization.
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validate(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            return authService.validarToken(authHeader);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MensajeResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error interno al validar el token: " + e.getMessage()));
        }
    }

    /**
     * POST /api/auth/recuperar-clave
     * Solicita token temporal de recuperación de contraseña.
     */
    @PostMapping("/recuperar-clave")
    public ResponseEntity<?> recuperarClave(@Valid @RequestBody RecuperarClaveDto dto,
                                           BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return extraerErroresValidacion(bindingResult);
            }
            return authService.solicitarRecuperacionClave(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MensajeResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error interno al solicitar recuperación de clave: " + e.getMessage()));
        }
    }

    /**
     * POST /api/auth/restablecer-clave
     * Confirma el restablecimiento de la contraseña con el token temporal.
     */
    @PostMapping("/restablecer-clave")
    public ResponseEntity<?> restablecerClave(@Valid @RequestBody RestablecerClaveDto dto,
                                             BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return extraerErroresValidacion(bindingResult);
            }
            return authService.restablecerClave(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MensajeResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error interno al restablecer la contraseña: " + e.getMessage()));
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
                errores
        ));
    }
}
