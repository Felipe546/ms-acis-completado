package com.acis.auth_users.model;

/**
 * Roles del sistema ACIS.
 * Define las atribuciones y el control de acceso en el servidor.
 */
public enum Rol {
    ADMINISTRADOR("Administrador del sistema - Acceso total"),
    PLANIFICADOR("Planificador de operaciones - Gestión de órdenes y visualización técnica"),
    TECNICO("Técnico de terreno - Ejecución de órdenes y perfil personal"),
    CLIENTE("Cliente solicitante - Seguimiento y perfil personal");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static Rol fromString(String rolStr) {
        if (rolStr == null || rolStr.trim().isEmpty()) {
            return null;
        }
        String clean = rolStr.trim().toUpperCase();
        for (Rol r : values()) {
            if (r.name().equalsIgnoreCase(clean)) {
                return r;
            }
        }
        return null;
    }
}
