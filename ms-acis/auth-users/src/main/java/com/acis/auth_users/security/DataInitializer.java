package com.acis.auth_users.security;

import com.acis.auth_users.model.Rol;
import com.acis.auth_users.model.Usuario;
import com.acis.auth_users.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            logger.info("Inicializando usuarios de prueba en base de datos H2...");

            crearUsuarioSiNoExiste("admin@acis.cl", "11111111-1", "Administrador General",
                    "Admin123!", Rol.ADMINISTRADOR, true);

            crearUsuarioSiNoExiste("planificador@acis.cl", "22222222-2", "Planificador de Operaciones",
                    "Planif123!", Rol.PLANIFICADOR, true);

            crearUsuarioSiNoExiste("tecnico@acis.cl", "33333333-3", "Técnico de Terreno",
                    "Tecnico123!", Rol.TECNICO, true);

            crearUsuarioSiNoExiste("cliente@acis.cl", "44444444-4", "Cliente Solicitante",
                    "Cliente123!", Rol.CLIENTE, true);

            // Usuario inactivo para validación de cuentas deshabilitadas
            crearUsuarioSiNoExiste("inactivo@acis.cl", "55555555-5", "Usuario Desactivado",
                    "Inactivo123!", Rol.CLIENTE, false);

            logger.info("Usuarios de prueba cargados exitosamente (4 roles activos + 1 inactivo)");
        }
    }

    private void crearUsuarioSiNoExiste(String email, String rut, String nombre,
                                       String clavePlana, Rol rol, boolean activo) {
        if (!usuarioRepository.existsByEmailIgnoreCase(email)) {
            Usuario u = new Usuario();
            u.setEmail(email);
            u.setRut(rut);
            u.setNombre(nombre);
            u.setClave(passwordEncoder.encode(clavePlana));
            u.setRol(rol);
            u.setActivo(activo);
            usuarioRepository.save(u);
            logger.info("Usuario creado: email='{}', rut='{}', rol='{}', activo={}", email, rut, rol, activo);
        }
    }
}
