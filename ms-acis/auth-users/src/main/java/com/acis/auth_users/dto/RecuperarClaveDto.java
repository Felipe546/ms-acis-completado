package com.acis.auth_users.dto;

import jakarta.validation.constraints.NotBlank;

public class RecuperarClaveDto {

    @NotBlank(message = "El identificador (correo o RUT) es requerido")
    private String identificador;

    public RecuperarClaveDto() {
    }

    public RecuperarClaveDto(String identificador) {
        this.identificador = identificador;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }
}
