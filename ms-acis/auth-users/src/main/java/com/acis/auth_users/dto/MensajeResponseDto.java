package com.acis.auth_users.dto;

import java.time.LocalDateTime;

public class MensajeResponseDto {

    private LocalDateTime timestamp = LocalDateTime.now();
    private int status;
    private String mensaje;
    private Object detalles;

    public MensajeResponseDto() {
    }

    public MensajeResponseDto(int status, String mensaje) {
        this.status = status;
        this.mensaje = mensaje;
    }

    public MensajeResponseDto(int status, String mensaje, Object detalles) {
        this.status = status;
        this.mensaje = mensaje;
        this.detalles = detalles;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Object getDetalles() {
        return detalles;
    }

    public void setDetalles(Object detalles) {
        this.detalles = detalles;
    }
}
