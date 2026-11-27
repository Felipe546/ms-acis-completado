package com.acis.ot_gestion.service;

import com.acis.ot_gestion.dto.IntervencionUpdateRequest;
import com.acis.ot_gestion.dto.OrdenRequest;
import com.acis.ot_gestion.model.EstadoOt;
import com.acis.ot_gestion.model.OrdenTrabajo;
import com.acis.ot_gestion.repository.OrdenTrabajoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class OrdenTrabajoService {
    private final OrdenTrabajoRepository repo;

    public OrdenTrabajoService(OrdenTrabajoRepository repo) {
        this.repo = repo;
    }

    public List<OrdenTrabajo> listar(String correo, String rol, EstadoOt estado) {
        if ("CLIENTE".equals(rol)) {
            return repo.findByClienteCorreoIgnoreCase(correo).stream()
                    .filter(o -> estado == null || o.getEstado() == estado).toList();
        }
        if ("TECNICO".equals(rol)) {
            return repo.findByTecnicoAsignadoIgnoreCase(correo).stream()
                    .filter(o -> estado == null || o.getEstado() == estado).toList();
        }
        return estado == null ? repo.findAll() : repo.findByEstado(estado);
    }

    public OrdenTrabajo buscar(String code, String correo, String rol) {
        var orden = porCodigo(code);
        verificarLectura(orden, correo, rol);
        return orden;
    }

    /** Las órdenes con técnico elegido nacen asignadas y disponibles para el técnico. */
    public OrdenTrabajo crear(OrdenRequest request) {
        var orden = new OrdenTrabajo();
        orden.setCodigoOt(siguienteCodigo());
        copiarDatos(orden, request);
        orden.setEstado(orden.getTecnicoAsignado() == null || orden.getTecnicoAsignado().isBlank()
                ? EstadoOt.CREADA : EstadoOt.ASIGNADA);
        return repo.save(orden);
    }

    /** Editar una orden creada con técnico elegido la deja lista para que el técnico la inicie. */
    public OrdenTrabajo actualizar(String code, OrdenRequest request) {
        var orden = buscar(code, "", "ADMINISTRADOR");
        if (orden.getEstado() != EstadoOt.CREADA && orden.getEstado() != EstadoOt.ASIGNADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se editan OTs creadas o asignadas");
        }
        copiarDatos(orden, request);
        if (orden.getEstado() == EstadoOt.CREADA
                && orden.getTecnicoAsignado() != null && !orden.getTecnicoAsignado().isBlank()) {
            orden.setEstado(EstadoOt.ASIGNADA);
        }
        return repo.save(orden);
    }

    public OrdenTrabajo asignar(String code, String tecnico) {
        var orden = porCodigo(code);
        if (orden.getEstado() != EstadoOt.CREADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se asignan OTs creadas");
        }
        if (tecnico == null || tecnico.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo del técnico requerido");
        }
        orden.setTecnicoAsignado(tecnico.trim().toLowerCase());
        orden.setEstado(EstadoOt.ASIGNADA);
        return repo.save(orden);
    }

    public OrdenTrabajo cambiarEstado(String code, EstadoOt nuevo, String correo, String rol,
                                      String comentario, String respuestaTecnico) {
        var orden = buscar(code, correo, rol);
        EstadoOt actual = orden.getEstado();
        boolean permitida = switch (actual) {
            case CREADA -> (nuevo == EstadoOt.ASIGNADA && "PLANIFICADOR".equals(rol))
                    || (nuevo == EstadoOt.ACEPTADA && "TECNICO".equals(rol));
            case ASIGNADA -> nuevo == EstadoOt.ACEPTADA && "TECNICO".equals(rol);
            case ACEPTADA -> nuevo == EstadoOt.EN_VISITA && "TECNICO".equals(rol);
            case EN_VISITA -> nuevo == EstadoOt.PENDIENTE_CONFORMIDAD && "TECNICO".equals(rol);
            case PENDIENTE_CONFORMIDAD -> (nuevo == EstadoOt.OBSERVADA || nuevo == EstadoOt.CERRADA)
                    && "CLIENTE".equals(rol);
            case OBSERVADA -> nuevo == EstadoOt.PENDIENTE_CONFORMIDAD && "TECNICO".equals(rol);
            case CERRADA -> false;
        };
        if (!permitida) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Transición de estado no permitida");
        }
        if (nuevo == EstadoOt.OBSERVADA) orden.setObservacionCliente(comentario);
        if (nuevo == EstadoOt.PENDIENTE_CONFORMIDAD && respuestaTecnico != null) {
            orden.setRespuestaTecnico(respuestaTecnico);
        }
        if (nuevo == EstadoOt.CERRADA) orden.setFechaConformidad(OffsetDateTime.now().toString());
        orden.setEstado(nuevo);
        return repo.save(orden);
    }

    /** Copia el informe de terain-photo a la misma OT que consulta la aplicación y Postman. */
    public OrdenTrabajo actualizarIntervencion(String code, String correo, String rol,
                                                IntervencionUpdateRequest request) {
        var orden = porCodigo(code);
        if (!"TECNICO".equals(rol) || orden.getTecnicoAsignado() == null
                || !orden.getTecnicoAsignado().equalsIgnoreCase(correo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La OT no está asignada a este técnico");
        }
        if (orden.getEstado() != EstadoOt.EN_VISITA && orden.getEstado() != EstadoOt.PENDIENTE_CONFORMIDAD) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La OT no está en una etapa de intervención");
        }
        if (request.checkIn() != null) orden.setCheckIn(request.checkIn());
        if (request.ubicacionCheckIn() != null) orden.setUbicacionCheckIn(request.ubicacionCheckIn());
        if (request.checkOut() != null) orden.setCheckOut(request.checkOut());
        if (request.diagnostico() != null) orden.setDiagnostico(request.diagnostico());
        if (request.actividad() != null) orden.setActividad(request.actividad());
        if (request.materiales() != null) orden.setMateriales(request.materiales());
        if (request.fotoEvidencia() != null) orden.setFotoEvidencia(request.fotoEvidencia());
        return repo.save(orden);
    }

    public void borrar(String code) {
        var orden = buscar(code, "", "ADMINISTRADOR");
        if (orden.getEstado() != EstadoOt.CREADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se eliminan OTs creadas");
        }
        repo.delete(orden);
    }

    private OrdenTrabajo porCodigo(String codigo) {
        return repo.findByCodigoOt(codigo).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OT no encontrada"));
    }

    private String siguienteCodigo() {
        long numero = 1001;
        while (repo.existsByCodigoOt("OT-" + numero)) numero++;
        return "OT-" + numero;
    }

    private void copiarDatos(OrdenTrabajo orden, OrdenRequest request) {
        orden.setClienteCorreo(request.clienteCorreo().trim().toLowerCase());
        orden.setLocal(request.local().trim());
        orden.setEquipo(request.equipo().trim());
        orden.setDescripcion(request.descripcion().trim());
        orden.setFechaProgramada(request.fechaProgramada().trim());
        orden.setTipo(request.tipo());
        orden.setPrioridad(request.prioridad());
        if (request.tecnicoAsignado() != null) {
            orden.setTecnicoAsignado(request.tecnicoAsignado().trim().toLowerCase());
        }
    }

    private void verificarLectura(OrdenTrabajo orden, String correo, String rol) {
        if ("CLIENTE".equals(rol) && !orden.getClienteCorreo().equalsIgnoreCase(correo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso a esta OT");
        }
        if ("TECNICO".equals(rol) && (orden.getTecnicoAsignado() == null
                || !orden.getTecnicoAsignado().equalsIgnoreCase(correo))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso a esta OT");
        }
    }
}
