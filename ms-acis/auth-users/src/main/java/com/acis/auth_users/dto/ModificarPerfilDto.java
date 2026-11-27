package com.acis.auth_users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class ModificarPerfilDto {

    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Email(message = "El formato de correo electrónico no es válido")
    @Size(max = 100, message = "El correo no puede exceder 100 caracteres")
    private String email;

    private String claveActual;

    @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
    private String nuevaClave;

    public ModificarPerfilDto() {
    }

    public ModificarPerfilDto(String nombre, String email, String claveActual, String nuevaClave) {
        this.nombre = nombre;
        this.email = email;
        this.claveActual = claveActual;
        this.nuevaClave = nuevaClave;
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

    public String getClaveActual() {
        return claveActual;
    }

    public void setClaveActual(String claveActual) {
        this.claveActual = claveActual;
    }

    public String getNuevaClave() {
        return nuevaClave;
    }

    public void setNuevaClave(String nuevaClave) {
        this.nuevaClave = nuevaClave;
    }
}
