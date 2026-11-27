package com.acis.terain_photo.service;

import com.acis.terain_photo.model.Intervencion;
import com.acis.terain_photo.repository.IntervencionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class IntervencionService {
    private final IntervencionRepository repo;
    private final RestClient ordenes;

    public IntervencionService(IntervencionRepository repo, RestClient ordenesRestClient) {
        this.repo = repo;
        this.ordenes = ordenesRestClient;
    }

    public Intervencion checkIn(String code, String geo, String email, String auth) {
        String estado = verificarAsignacion(code, email, auth);
        if (!"ACEPTADA".equals(estado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La OT debe estar aceptada antes del check-in");
        }
        var intervencion = repo.findByCodigoOt(code).orElseGet(Intervencion::new);
        if (intervencion.getCheckIn() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Check-in ya registrado");
        }
        intervencion.setCodigoOt(code);
        intervencion.setTecnicoEmail(email);
        intervencion.setCheckIn(OffsetDateTime.now().toString());
        intervencion.setUbicacionCheckIn(geo);

        actualizarEstado(code, "EN_VISITA", auth);
        var guardada = repo.save(intervencion);
        Map<String, Object> datos = new HashMap<>();
        datos.put("checkIn", guardada.getCheckIn());
        datos.put("ubicacionCheckIn", geo == null ? "" : geo);
        actualizarDatosOrden(code, auth, datos);
        return guardada;
    }

    public Intervencion informe(String code, String diagnostico, String actividad,
                                String materiales, String email, String auth) {
        String estado = verificarAsignacion(code, email, auth);
        if (!"EN_VISITA".equals(estado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La OT no está en visita");
        }
        var intervencion = obtenerPropia(code, email);
        if (intervencion.getCheckIn() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Registra check-in antes del informe");
        }
        intervencion.setDiagnostico(diagnostico.trim());
        intervencion.setActividad(actividad.trim());
        intervencion.setMateriales(materiales == null ? "" : materiales.trim());
        intervencion.setCheckOut(OffsetDateTime.now().toString());

        actualizarEstado(code, "PENDIENTE_CONFORMIDAD", auth);
        var guardada = repo.save(intervencion);
        Map<String, Object> datos = new HashMap<>();
        datos.put("checkOut", guardada.getCheckOut());
        datos.put("diagnostico", guardada.getDiagnostico());
        datos.put("actividad", guardada.getActividad());
        datos.put("materiales", guardada.getMateriales());
        actualizarDatosOrden(code, auth, datos);
        return guardada;
    }

    public void actualizarFotoEnOrdenes(String code, String nombre, String auth) {
        actualizarDatosOrden(code, auth, Map.of("fotoEvidencia", nombre));
    }

    private String verificarAsignacion(String code, String email, String auth) {
        try {
            Map<?, ?> orden = ordenes.get().uri("/api/ordenes/{code}", code)
                    .header("Authorization", auth).retrieve().body(Map.class);
            if (orden == null) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Respuesta vacía de ot-gestion");
            }
            if (!email.equalsIgnoreCase(String.valueOf(orden.get("tecnicoAsignado")))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "La OT no está asignada a este técnico");
            }
            return String.valueOf(orden.get("estado"));
        } catch (org.springframework.web.client.HttpClientErrorException error) {
            throw new ResponseStatusException(error.getStatusCode(),
                    "ot-gestion rechazó la operación");
        } catch (ResponseStatusException error) {
            throw error;
        } catch (Exception error) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo validar la OT en ot-gestion");
        }
    }

    public void validarAccesoLectura(String code, String auth) {
        try {
            ordenes.get().uri("/api/ordenes/{code}", code)
                    .header("Authorization", auth).retrieve().toBodilessEntity();
        } catch (org.springframework.web.client.HttpClientErrorException error) {
            throw new ResponseStatusException(error.getStatusCode(), "No tienes acceso a la evidencia");
        } catch (Exception error) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo validar el acceso a la OT");
        }
    }

    public String verificarAsignacionParaFoto(String code, String email, String auth) {
        return verificarAsignacion(code, email, auth);
    }

    public Intervencion porFoto(String name) {
        return repo.findByFotoEvidencia(name).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Evidencia sin intervención asociada"));
    }

    public Intervencion obtenerPropia(String code, String email) {
        var intervencion = repo.findByCodigoOt(code).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Intervención no encontrada"));
        if (!email.equalsIgnoreCase(intervencion.getTecnicoEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "La intervención pertenece a otro técnico");
        }
        return intervencion;
    }

    public Intervencion save(Intervencion intervencion) {
        return repo.save(intervencion);
    }

    private void actualizarEstado(String code, String estado, String auth) {
        try {
            ordenes.patch().uri("/api/ordenes/{code}/estado", code)
                    .header("Authorization", auth)
                    .body(Map.of("estado", estado)).retrieve().toBodilessEntity();
        } catch (Exception error) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo actualizar el estado en ot-gestion");
        }
    }

    private void actualizarDatosOrden(String code, String auth, Map<String, ?> datos) {
        try {
            ordenes.put().uri("/api/ordenes/{code}/intervencion", code)
                    .header("Authorization", auth).body(datos).retrieve().toBodilessEntity();
        } catch (org.springframework.web.client.HttpClientErrorException error) {
            throw new ResponseStatusException(error.getStatusCode(),
                    "ot-gestion rechazó la actualización de la intervención");
        } catch (Exception error) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo sincronizar la intervención con ot-gestion");
        }
    }
}
