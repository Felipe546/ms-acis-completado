package com.acis.auth_users.repository;

import com.acis.auth_users.model.Rol;
import com.acis.auth_users.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByRutIgnoreCase(String rut);

    @Query("SELECT u FROM Usuario u WHERE LOWER(u.email) = LOWER(:identificador) OR LOWER(u.rut) = LOWER(:identificador)")
    Optional<Usuario> findByIdentificador(@Param("identificador") String identificador);

    Optional<Usuario> findByResetToken(String resetToken);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRutIgnoreCase(String rut);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByRutIgnoreCaseAndIdNot(String rut, Long id);

    List<Usuario> findByRol(Rol rol);

    List<Usuario> findByActivo(Boolean activo);

    List<Usuario> findByRolAndActivo(Rol rol, Boolean activo);

    List<Usuario> findByRolIn(Collection<Rol> roles);

    List<Usuario> findByRolInAndActivo(Collection<Rol> roles, Boolean activo);
}
