package com.acis.auth_users.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequestDto {

    @NotBlank(message = "El identificador (correo o RUT) es requerido")
    private String identificador;

    @NotBlank(message = "La contraseña es requerida")
    private String clave;

    public LoginRequestDto() {
    }

    public LoginRequestDto(String identificador, String clave) {
        this.identificador = identificador;
        this.clave = clave;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }
}
