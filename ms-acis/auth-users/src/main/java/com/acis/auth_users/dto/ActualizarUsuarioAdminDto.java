package com.acis.auth_users.dto;

import com.acis.auth_users.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class ActualizarUsuarioAdminDto {

    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Email(message = "El formato de correo electrónico no es válido")
    @Size(max = 100, message = "El correo no puede exceder 100 caracteres")
    private String email;

    private String rut;

    private Rol rol;

    private Boolean activo;

    @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
    private String nuevaClave;

    public ActualizarUsuarioAdminDto() {
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

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
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

    public String getNuevaClave() {
        return nuevaClave;
    }

    public void setNuevaClave(String nuevaClave) {
        this.nuevaClave = nuevaClave;
    }
}
