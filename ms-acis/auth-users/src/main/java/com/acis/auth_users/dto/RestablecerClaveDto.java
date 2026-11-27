package com.acis.auth_users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RestablecerClaveDto {

    @NotBlank(message = "El token de recuperación es requerido")
    private String token;

    @NotBlank(message = "La nueva contraseña es requerida")
    @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
    private String nuevaClave;

    public RestablecerClaveDto() {
    }

    public RestablecerClaveDto(String token, String nuevaClave) {
        this.token = token;
        this.nuevaClave = nuevaClave;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNuevaClave() {
        return nuevaClave;
    }

    public void setNuevaClave(String nuevaClave) {
        this.nuevaClave = nuevaClave;
    }
}
