package com.acis.auth_users.service;

import com.acis.auth_users.dto.LoginRequestDto;
import com.acis.auth_users.dto.LoginResponseDto;
import com.acis.auth_users.dto.MensajeResponseDto;
import com.acis.auth_users.dto.RecuperarClaveDto;
import com.acis.auth_users.dto.RegistroUsuarioDto;
import com.acis.auth_users.dto.RestablecerClaveDto;
import com.acis.auth_users.dto.UsuarioResponseDto;
import com.acis.auth_users.dto.ValidarTokenResponseDto;
import com.acis.auth_users.model.Rol;
import com.acis.auth_users.model.Usuario;
import com.acis.auth_users.repository.UsuarioRepository;
import com.acis.auth_users.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Autenticación de usuario por correo o RUT.
     * Retorna token JWT y datos de identidad sin exponer la contraseña.
     */
    public ResponseEntity<?> login(LoginRequestDto dto) {
        if (dto.getIdentificador() == null || dto.getIdentificador().trim().isEmpty() ||
                dto.getClave() == null || dto.getClave().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MensajeResponseDto(HttpStatus.BAD_REQUEST.value(), "El identificador y la contraseña son obligatorios"));
        }

        String identificador = dto.getIdentificador().trim();
        Optional<Usuario> usuarioOpt = buscarPorIdentificador(identificador);

        if (usuarioOpt.isEmpty()) {
            logger.warn("Intento de inicio de sesión fallido: usuario no encontrado para identificador '{}'", identificador);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MensajeResponseDto(HttpStatus.UNAUTHORIZED.value(), "Credenciales inválidas"));
        }

        Usuario usuario = usuarioOpt.get();

        // Validación de cuenta inactiva
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            logger.warn("Intento de inicio de sesión en cuenta inactiva: '{}'", usuario.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MensajeResponseDto(HttpStatus.UNAUTHORIZED.value(), "La cuenta se encuentra inactiva. Contacte al administrador"));
        }

        // Validación de contraseña
        if (!passwordEncoder.matches(dto.getClave(), usuario.getClave())) {
            logger.warn("Intento de inicio de sesión con clave incorrecta para: '{}'", usuario.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MensajeResponseDto(HttpStatus.UNAUTHORIZED.value(), "Credenciales inválidas"));
        }

        // Generar JWT
        String token = jwtUtil.generarToken(usuario);

        LoginResponseDto responseDto = new LoginResponseDto(
                token,
                usuario.getId(),
                usuario.getRut(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getActivo()
        );

        logger.info("Inicio de sesión exitoso para usuario '{}' con rol '{}'", usuario.getEmail(), usuario.getRol());
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Registro de nuevo usuario con validación y cifrado de clave.
     * Si no es administrador, solo se permite registrar rol CLIENTE.
     */
    @Transactional
    public ResponseEntity<?> register(RegistroUsuarioDto dto, Authentication authentication) {
        // Validación de datos requeridos
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MensajeResponseDto(HttpStatus.BAD_REQUEST.value(), "El nombre es obligatorio"));
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MensajeResponseDto(HttpStatus.BAD_REQUEST.value(), "El correo electrónico es obligatorio"));
        }
        if (dto.getRut() == null || dto.getRut().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MensajeResponseDto(HttpStatus.BAD_REQUEST.value(), "El RUT es obligatorio"));
        }
        if (dto.getClave() == null || dto.getClave().trim().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(new MensajeResponseDto(HttpStatus.BAD_REQUEST.value(), "La contraseña debe tener al menos 6 caracteres"));
        }

        String email = dto.getEmail().trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ResponseEntity.badRequest()
                    .body(new MensajeResponseDto(HttpStatus.BAD_REQUEST.value(), "Formato de correo electrónico inválido"));
        }

        String rutNormalizado = normalizarRut(dto.getRut().trim());
        if (!validarFormatoRut(rutNormalizado)) {
            return ResponseEntity.badRequest()
                    .body(new MensajeResponseDto(HttpStatus.BAD_REQUEST.value(), "Formato de RUT chileno inválido (ej: 12345678-9)"));
        }

        // Validación de duplicados
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MensajeResponseDto(HttpStatus.CONFLICT.value(), "El correo electrónico ya se encuentra registrado"));
        }

        if (usuarioRepository.existsByRutIgnoreCase(rutNormalizado)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MensajeResponseDto(HttpStatus.CONFLICT.value(), "El RUT ya se encuentra registrado"));
        }

        // Control de roles en servidor
        Rol rolAsignado = Rol.CLIENTE;
        boolean esAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        if (dto.getRol() != null) {
            if (dto.getRol() != Rol.CLIENTE && !esAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MensajeResponseDto(HttpStatus.FORBIDDEN.value(),
                                "Solo los administradores pueden asignar roles internos (ADMINISTRADOR, PLANIFICADOR, TECNICO)"));
            }
            rolAsignado = dto.getRol();
        }

        // Creación y cifrado de usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(dto.getNombre().trim());
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setRut(rutNormalizado);
        nuevoUsuario.setClave(passwordEncoder.encode(dto.getClave().trim()));
        nuevoUsuario.setRol(rolAsignado);
        nuevoUsuario.setActivo(true);

        Usuario guardado = usuarioRepository.save(nuevoUsuario);
        logger.info("Usuario registrado exitosamente: id={}, email={}, rol={}", guardado.getId(), guardado.getEmail(), guardado.getRol());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UsuarioResponseDto.fromEntity(guardado));
    }

    /**
     * Valida la validez y vigencia de un token JWT.
     */
    public ResponseEntity<?> validarToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ValidarTokenResponseDto(false, "Token ausente o malformado", null));
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validarToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ValidarTokenResponseDto(false, "Token inválido o expirado", null));
        }

        String email = jwtUtil.obtenerEmail(token);
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailIgnoreCase(email);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ValidarTokenResponseDto(false, "Usuario del token no encontrado", null));
        }

        Usuario usuario = usuarioOpt.get();
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ValidarTokenResponseDto(false, "La cuenta del usuario está desactivada", null));
        }

        return ResponseEntity.ok(new ValidarTokenResponseDto(
                true,
                "Token válido",
                UsuarioResponseDto.fromEntity(usuario)
        ));
    }

    /**
     * Solicita token temporal de recuperación de contraseña.
     */
    @Transactional
    public ResponseEntity<?> solicitarRecuperacionClave(RecuperarClaveDto dto) {
        if (dto.getIdentificador() == null || dto.getIdentificador().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MensajeResponseDto(HttpStatus.BAD_REQUEST.value(), "El identificador es obligatorio"));
        }

        String identificador = dto.getIdentificador().trim();
        Optional<Usuario> usuarioOpt = buscarPorIdentificador(identificador);

        Map<String, Object> detalles = new LinkedHashMap<>();
        detalles.put("expiracionMinutos", 30);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (Boolean.TRUE.equals(usuario.getActivo())) {
                String tokenRecuperacion = UUID.randomUUID().toString();
                usuario.setResetToken(tokenRecuperacion);
                usuario.setResetTokenExpiracion(LocalDateTime.now().plusMinutes(30));
                usuarioRepository.save(usuario);

                // El token debe entregarse mediante un canal verificado (correo/SMS)
                // al integrar el proveedor de notificaciones. No se expone en la API.
            }
        }

        return ResponseEntity.ok(new MensajeResponseDto(
                HttpStatus.OK.value(),
                "Si los datos corresponden a un usuario activo registrado, se han generado las instrucciones para restablecer su contraseña",
                detalles
        ));
    }

    /**
     * Restablece la contraseña utilizando el token temporal generado.
     */
    @Transactional
    public ResponseEntity<?> restablecerClave(RestablecerClaveDto dto) {
        if (dto.getToken() == null || dto.getToken().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MensajeResponseDto(HttpStatus.BAD_REQUEST.value(), "El token de recuperación es obligatorio"));
        }
        if (dto.getNuevaClave() == null || dto.getNuevaClave().trim().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(new MensajeResponseDto(HttpStatus.BAD_REQUEST.value(), "La nueva contraseña debe tener al menos 6 caracteres"));
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByResetToken(dto.getToken().trim());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MensajeResponseDto(HttpStatus.BAD_REQUEST.value(), "Token de recuperación inválido o inexistente"));
        }

        Usuario usuario = usuarioOpt.get();

        if (usuario.getResetTokenExpiracion() == null || usuario.getResetTokenExpiracion().isBefore(LocalDateTime.now())) {
            usuario.setResetToken(null);
            usuario.setResetTokenExpiracion(null);
            usuarioRepository.save(usuario);
            return ResponseEntity.badRequest()
                    .body(new MensajeResponseDto(HttpStatus.BAD_REQUEST.value(), "El token de recuperación ha expirado. Solicite uno nuevo"));
        }

        usuario.setClave(passwordEncoder.encode(dto.getNuevaClave().trim()));
        usuario.setResetToken(null);
        usuario.setResetTokenExpiracion(null);
        usuarioRepository.save(usuario);

        logger.info("Contraseña restablecida exitosamente para el usuario: {}", usuario.getEmail());
        return ResponseEntity.ok(new MensajeResponseDto(
                HttpStatus.OK.value(),
                "Contraseña restablecida exitosamente. Ya puede iniciar sesión con su nueva clave"
        ));
    }

    private Optional<Usuario> buscarPorIdentificador(String identificador) {
        // Si contiene '@', se busca prioritariamente por correo
        if (identificador.contains("@")) {
            return usuarioRepository.findByEmailIgnoreCase(identificador);
        }

        // Búsqueda por RUT normalizado o directo
        String rutNorm = normalizarRut(identificador);
        Optional<Usuario> porRut = usuarioRepository.findByRutIgnoreCase(rutNorm);
        if (porRut.isPresent()) {
            return porRut;
        }

        // Búsqueda genérica por identificador
        return usuarioRepository.findByIdentificador(identificador);
    }

    public static String normalizarRut(String rut) {
        if (rut == null) {
            return "";
        }
        String clean = rut.replace(".", "").replace(" ", "").trim().toUpperCase();
        if (!clean.contains("-") && clean.length() > 1) {
            int len = clean.length();
            return clean.substring(0, len - 1) + "-" + clean.substring(len - 1);
        }
        return clean;
    }

    public static boolean validarFormatoRut(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return false;
        }
        return rut.matches("^\\d{7,8}-[\\dK]$");
    }
}
