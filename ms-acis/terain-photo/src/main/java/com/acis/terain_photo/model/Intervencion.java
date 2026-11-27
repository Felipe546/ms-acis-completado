package com.acis.terain_photo.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "intervenciones")
public class Intervencion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String codigoOt;
    private String tecnicoEmail;
    private String checkIn, ubicacionCheckIn, checkOut;
    @Column(length = 3000)
    private String diagnostico, actividad, materiales;
    private String fotoEvidencia;
    private Long actualizada = Instant.now().toEpochMilli();

    public Long getId() {
        return id;
    }

    public String getCodigoOt() {
        return codigoOt;
    }

    public void setCodigoOt(String x) {
        codigoOt = x;
    }

    public String getTecnicoEmail() {
        return tecnicoEmail;
    }

    public void setTecnicoEmail(String x) {
        tecnicoEmail = x;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(String x) {
        checkIn = x;
    }

    public String getUbicacionCheckIn() {
        return ubicacionCheckIn;
    }

    public void setUbicacionCheckIn(String x) {
        ubicacionCheckIn = x;
    }

    public String getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(String x) {
        checkOut = x;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String x) {
        diagnostico = x;
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String x) {
        actividad = x;
    }

    public String getMateriales() {
        return materiales;
    }

    public void setMateriales(String x) {
        materiales = x;
    }

    public String getFotoEvidencia() {
        return fotoEvidencia;
    }

    public void setFotoEvidencia(String x) {
        fotoEvidencia = x;
    }

    public Long getActualizada() {
        return actualizada;
    }
}
