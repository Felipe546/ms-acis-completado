package com.acis.ot_gestion.dto;
import com.acis.ot_gestion.model.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public record OrdenRequest(@NotBlank String clienteCorreo,@NotBlank String local,@NotBlank String equipo,@NotBlank String descripcion,@NotBlank String fechaProgramada,@NotNull TipoOt tipo,@NotNull PrioridadOt prioridad,String tecnicoAsignado) {}
