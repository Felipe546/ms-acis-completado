package com.acis.auth_users.service;

import com.acis.auth_users.dto.ActualizarUsuarioAdminDto;
import com.acis.auth_users.dto.MensajeResponseDto;
import com.acis.auth_users.dto.ModificarPerfilDto;
import com.acis.auth_users.dto.UsuarioResponseDto;
import com.acis.auth_users.model.Rol;
import com.acis.auth_users.model.Usuario;
import com.acis.auth_users.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Lista usuarios según la matriz de permisos de servidor:
     * - ADMINISTRADOR: Puede ver todos los usuarios o filtrar por rol y estado.
     * - PLANIFICADOR: Puede ver técnicos, clientes y planificadores para asignación de órdenes.
     * - TECNICO / CLIENTE: Acceso denegado (403 Forbidden).
     */
    public ResponseEntity<?> listarUsuarios(Authentication authentication, Rol filtroRol, Boolean filtroActivo) {
        Usuario usuarioAutenticado = obtenerUsuarioAutenticado(authentication);
        if (usuarioAutenticado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MensajeResponseDto(HttpStatus.UNAUTHORIZED.value(), "Usuario no autenticado"));
        }

        Rol rolActual = usuarioAutenticado.getRol();

        if (rolActual == Rol.ADMINISTRADOR) {
            List<Usuario> usuarios;
            if (filtroRol != null && filtroActivo != null) {
                usuarios = usuarioRepository.findByRolAndActivo(filtroRol, filtroActivo);
            } else if (filtroRol != null) {
                usuarios = usuarioRepository.findByRol(filtroRol);
            } else if (filtroActivo != null) {
                usuarios = usuarioRepository.findByActivo(filtroActivo);
            } else {
                usuarios = usuarioRepository.findAll();
            }

            List<UsuarioResponseDto> dtos = usuarios.stream()
                    .map(UsuarioResponseDto::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        }

        if (rolActual == Rol.PLANIFICADOR) {
            // El planificador necesita listar técnicos y clientes (y planificadores) para coordinar las OT
            List<Rol> rolesPermitidos = Arrays.asList(Rol.TECNICO, Rol.CLIENTE, Rol.PLANIFICADOR);
            List<Usuario> usuarios;

            if (filtroRol != null) {
                if (!rolesPermitidos.contains(filtroRol)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MensajeResponseDto(HttpStatus.FORBIDDEN.value(),
                                    "No tiene permisos para consultar usuarios con rol: " + filtroRol));
                }
                usuarios = (filtroActivo != null)
                        ? usuarioRepository.findByRolAndActivo(filtroRol, filtroActivo)
                        : usuarioRepository.findByRol(filtroRol);
            } else {
                usuarios = (filtroActivo != null)
                        ? usuarioRepository.findByRolInAndActivo(rolesPermitidos, filtroActivo)
                        : usuarioRepository.findByRolIn(rolesPermitidos);
            }

            List<UsuarioResponseDto> dtos = usuarios.stream()
                    .map(UsuarioResponseDto::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        }

        // TECNICO y CLIENTE no tienen acceso al directorio de usuarios
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MensajeResponseDto(HttpStatus.FORBIDDEN.value(),
                        "No cuenta con permisos para listar usuarios. Su rol solo puede consultar su propio perfil"));
    }

    /**
     * Obtiene el perfil del usuario autenticado.
     */
    public ResponseEntity<?> obtenerPerfil(Authentication authentication) {
        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MensajeResponseDto(HttpStatus.UNAUTHORIZED.value(), "Usuario no autenticado"));
        }
        return ResponseEntity.ok(UsuarioResponseDto.fromEntity(usuario));
    }

    /**
     * Modificación de perfil propio.
     * Permite actualizar nombre, correo y contraseña previa verificación de la clave actual.
     * Nunca expone la clave en la respuesta.
     */
    @Transactional
    public ResponseEntity<?> modificarPerfil(Authentication authentication, ModificarPerfilDto dto) {
        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MensajeResponseDto(HttpStatus.UNAUTHORIZED.value(), "Usuario no autenticado"));
        }

        // Actualizar nombre
        if (dto.getNombre() != null && !dto.getNombre().trim().isEmpty()) {
            usuario.setNombre(dto.getNombre().trim());
        }

        // Actualizar correo
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            String nuevoEmail = dto.getEmail().trim().toLowerCase();
            if (!nuevoEmail.equalsIgnoreCase(usuario.getEmail())) {
                if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(nuevoEmail, usuario.getId())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new MensajeResponseDto(HttpStatus.CONFLICT.value(), "El correo ya se encuentra en uso por otro usuario"));
                }
                usuario.setEmail(nuevoEmail);
            }
        }

        // Cambio de contraseña
        if (dto.getNuevaClave() != null && !dto.getNuevaClave().trim().isEmpty()) {
            if (dto.getClaveActual() == null || dto.getClaveActual().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MensajeResponseDto(HttpStatus.BAD_REQUEST.value(), "Debe ingresar su contraseña actual para establecer una nueva"));
            }
            if (!passwordEncoder.matches(dto.getClaveActual(), usuario.getClave())) {
                return ResponseEntity.badRequest()
                        .body(new MensajeResponseDto(HttpStatus.BAD_REQUEST.value(), "La contraseña actual no es correcta"));
            }
            if (dto.getNuevaClave().trim().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(new MensajeResponseDto(HttpStatus.BAD_REQUEST.value(), "La nueva contraseña debe tener al menos 6 caracteres"));
            }
            usuario.setClave(passwordEncoder.encode(dto.getNuevaClave().trim()));
            logger.info("Contraseña actualizada por el propio usuario: {}", usuario.getEmail());
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        return ResponseEntity.ok(UsuarioResponseDto.fromEntity(actualizado));
    }

    /**
     * Obtiene un usuario específico por ID según permisos:
     * - ADMINISTRADOR: Puede ver a cualquier usuario.
     * - El mismo usuario: Puede ver su propia información.
     * - PLANIFICADOR: Puede ver técnicos y clientes.
     */
    public ResponseEntity<?> obtenerPorId(Long id, Authentication authentication) {
        Usuario usuarioAutenticado = obtenerUsuarioAutenticado(authentication);
        if (usuarioAutenticado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MensajeResponseDto(HttpStatus.UNAUTHORIZED.value(), "Usuario no autenticado"));
        }

        Optional<Usuario> usuarioTargetOpt = usuarioRepository.findById(id);
        if (usuarioTargetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensajeResponseDto(HttpStatus.NOT_FOUND.value(), "Usuario no encontrado con ID: " + id));
        }

        Usuario target = usuarioTargetOpt.get();

        boolean esPropio = usuarioAutenticado.getId().equals(target.getId());
        boolean esAdmin = usuarioAutenticado.getRol() == Rol.ADMINISTRADOR;
        boolean esPlanificadorViendoTecnicoOCliente = usuarioAutenticado.getRol() == Rol.PLANIFICADOR &&
                (target.getRol() == Rol.TECNICO || target.getRol() == Rol.CLIENTE);

        if (!esPropio && !esAdmin && !esPlanificadorViendoTecnicoOCliente) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MensajeResponseDto(HttpStatus.FORBIDDEN.value(), "No cuenta con permisos para ver este usuario"));
        }

        return ResponseEntity.ok(UsuarioResponseDto.fromEntity(target));
    }

    /**
     * Modificación de usuario por parte del ADMINISTRADOR.
     */
    @Transactional
    public ResponseEntity<?> actualizarUsuarioAdmin(Long id, ActualizarUsuarioAdminDto dto, Authentication authentication) {
        Usuario usuarioAutenticado = obtenerUsuarioAutenticado(authentication);
        if (usuarioAutenticado == null || usuarioAutenticado.getRol() != Rol.ADMINISTRADOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MensajeResponseDto(HttpStatus.FORBIDDEN.value(), "Solo los administradores pueden modificar usuarios directamente"));
        }

        Optional<Usuario> targetOpt = usuarioRepository.findById(id);
        if (targetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensajeResponseDto(HttpStatus.NOT_FOUND.value(), "Usuario no encontrado con ID: " + id));
        }

        Usuario target = targetOpt.get();

        if (dto.getNombre() != null && !dto.getNombre().trim().isEmpty()) {
            target.setNombre(dto.getNombre().trim());
        }

        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            String email = dto.getEmail().trim().toLowerCase();
            if (!email.equalsIgnoreCase(target.getEmail())) {
                if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new MensajeResponseDto(HttpStatus.CONFLICT.value(), "El correo ya está registrado en otro usuario"));
                }
                target.setEmail(email);
            }
        }

        if (dto.getRut() != null && !dto.getRut().trim().isEmpty()) {
            String rutNorm = AuthService.normalizarRut(dto.getRut().trim());
            if (!rutNorm.equalsIgnoreCase(target.getRut())) {
                if (usuarioRepository.existsByRutIgnoreCaseAndIdNot(rutNorm, id)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new MensajeResponseDto(HttpStatus.CONFLICT.value(), "El RUT ya está registrado en otro usuario"));
                }
                target.setRut(rutNorm);
            }
        }

        if (dto.getRol() != null) {
            target.setRol(dto.getRol());
        }

        if (dto.getActivo() != null) {
            if (target.getId().equals(usuarioAutenticado.getId()) && Boolean.FALSE.equals(dto.getActivo())) {
                return ResponseEntity.badRequest()
                        .body(new MensajeResponseDto(HttpStatus.BAD_REQUEST.value(), "Un administrador no puede desactivar su propia cuenta"));
            }
            target.setActivo(dto.getActivo());
        }

        if (dto.getNuevaClave() != null && !dto.getNuevaClave().trim().isEmpty()) {
            if (dto.getNuevaClave().trim().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(new MensajeResponseDto(HttpStatus.BAD_REQUEST.value(), "La contraseña debe tener al menos 6 caracteres"));
            }
            target.setClave(passwordEncoder.encode(dto.getNuevaClave().trim()));
            logger.info("Contraseña de usuario {} restablecida por administrador {}", target.getEmail(), usuarioAutenticado.getEmail());
        }

        Usuario guardado = usuarioRepository.save(target);
        return ResponseEntity.ok(UsuarioResponseDto.fromEntity(guardado));
    }

    /**
     * Activación o desactivación de cuenta de usuario (exclusivo ADMINISTRADOR).
     */
    @Transactional
    public ResponseEntity<?> cambiarEstado(Long id, Boolean activo, Authentication authentication) {
        Usuario usuarioAutenticado = obtenerUsuarioAutenticado(authentication);
        if (usuarioAutenticado == null || usuarioAutenticado.getRol() != Rol.ADMINISTRADOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MensajeResponseDto(HttpStatus.FORBIDDEN.value(), "Solo los administradores pueden cambiar el estado de un usuario"));
        }

        Optional<Usuario> targetOpt = usuarioRepository.findById(id);
        if (targetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensajeResponseDto(HttpStatus.NOT_FOUND.value(), "Usuario no encontrado con ID: " + id));
        }

        Usuario target = targetOpt.get();

        if (target.getId().equals(usuarioAutenticado.getId()) && Boolean.FALSE.equals(activo)) {
            return ResponseEntity.badRequest()
                    .body(new MensajeResponseDto(HttpStatus.BAD_REQUEST.value(), "Un administrador no puede desactivar su propia cuenta"));
        }

        target.setActivo(activo);
        Usuario guardado = usuarioRepository.save(target);
        logger.info("Estado de usuario {} cambiado a activo={}", guardado.getEmail(), guardado.getActivo());

        return ResponseEntity.ok(UsuarioResponseDto.fromEntity(guardado));
    }

    private Usuario obtenerUsuarioAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
    }
}
