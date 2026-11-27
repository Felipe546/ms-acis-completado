package com.acis.auth_users.dto;

import jakarta.validation.constraints.NotNull;

public class CambiarEstadoDto {

    @NotNull(message = "El estado activo es requerido")
    private Boolean activo;

    public CambiarEstadoDto() {
    }

    public CambiarEstadoDto(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
