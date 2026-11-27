package com.acis.ot_gestion.dto;

public record IntervencionUpdateRequest(
        String checkIn,
        String ubicacionCheckIn,
        String checkOut,
        String diagnostico,
        String actividad,
        String materiales,
        String fotoEvidencia) {
}
