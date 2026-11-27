package com.acis.auth_users.dto;

public class ValidarTokenResponseDto {

    private boolean valido;
    private String mensaje;
    private UsuarioResponseDto usuario;

    public ValidarTokenResponseDto() {
    }

    public ValidarTokenResponseDto(boolean valido, String mensaje, UsuarioResponseDto usuario) {
        this.valido = valido;
        this.mensaje = mensaje;
        this.usuario = usuario;
    }

    public boolean isValido() {
        return valido;
    }

    public void setValido(boolean valido) {
        this.valido = valido;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public UsuarioResponseDto getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioResponseDto usuario) {
        this.usuario = usuario;
    }
}
