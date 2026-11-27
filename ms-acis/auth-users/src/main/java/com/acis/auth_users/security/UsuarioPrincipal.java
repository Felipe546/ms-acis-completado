package com.acis.auth_users.security;

import com.acis.auth_users.model.Rol;
import com.acis.auth_users.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UsuarioPrincipal implements UserDetails {

    private final Long id;
    private final String rut;
    private final String nombre;
    private final String email;
    private final String clave;
    private final Rol rol;
    private final Boolean activo;
    private final Collection<? extends GrantedAuthority> authorities;

    public UsuarioPrincipal(Long id, String rut, String nombre, String email, String clave,
                            Rol rol, Boolean activo, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.rut = rut;
        this.nombre = nombre;
        this.email = email;
        this.clave = clave;
        this.rol = rol;
        this.activo = activo;
        this.authorities = authorities;
    }

    public static UsuarioPrincipal build(Usuario usuario) {
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name());
        return new UsuarioPrincipal(
                usuario.getId(),
                usuario.getRut(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getClave(),
                usuario.getRol(),
                usuario.getActivo(),
                Collections.singletonList(authority)
        );
    }

    public Long getId() {
        return id;
    }

    public String getRut() {
        return rut;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public Rol getRol() {
        return rol;
    }

    public Boolean getActivo() {
        return activo;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return clave;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return Boolean.TRUE.equals(activo);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(activo);
    }
}
