package com.acis.auth_users.dto;

import com.acis.auth_users.model.Rol;

public class LoginResponseDto {

    private String token;
    private String tipoToken = "Bearer";
    private Long id;
    private String rut;
    private String nombre;
    private String email;
    private Rol rol;
    private Boolean activo;

    public LoginResponseDto() {
    }

    public LoginResponseDto(String token, Long id, String rut, String nombre, String email, Rol rol, Boolean activo) {
        this.token = token;
        this.tipoToken = "Bearer";
        this.id = id;
        this.rut = rut;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.activo = activo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipoToken() {
        return tipoToken;
    }

    public void setTipoToken(String tipoToken) {
        this.tipoToken = tipoToken;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
