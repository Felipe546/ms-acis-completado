package com.acis.terain_photo.controller;

import com.acis.terain_photo.dto.*;
import com.acis.terain_photo.service.*;
import jakarta.validation.Valid;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/intervenciones")
public class IntervencionController {
    private final IntervencionService service;
    private final StorageService storage;

    public IntervencionController(IntervencionService s, StorageService st) {
        service = s;
        storage = st;
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('TECNICO')")
    public Object checkIn(@Valid @RequestBody CheckInRequest r, Authentication a,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return service.checkIn(r.codigoOt(), r.ubicacionCheckIn(), a.getName(), auth);
    }

    @PostMapping("/informe")
    @PreAuthorize("hasRole('TECNICO')")
    public Object informe(@Valid @RequestBody InformeRequest r, Authentication a,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return service.informe(r.codigoOt(), r.diagnostico(), r.actividad(), r.materiales(), a.getName(), auth);
    }

    @PostMapping(value = "/{codigoOt}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TECNICO')")
    public Object foto(@PathVariable String codigoOt, @RequestPart("file") MultipartFile file, Authentication a,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        if (!"EN_VISITA".equals(service.verificarAsignacionParaFoto(codigoOt, a.getName(), auth)))
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT,
                    "La OT no está en visita");
        var i = service.obtenerPropia(codigoOt, a.getName());
        var name = storage.guardar(file);
        i.setFotoEvidencia(name);
        var guardada = service.save(i);
        service.actualizarFotoEnOrdenes(codigoOt, name, auth);
        return guardada;
    }

    @GetMapping("/evidencias/{nombreFoto}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> foto(@PathVariable String nombreFoto, Authentication a,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        var i = service.porFoto(nombreFoto);
        service.validarAccesoLectura(i.getCodigoOt(), auth);
        var p = storage.resolver(nombreFoto);
        try {
            String ext = nombreFoto.substring(nombreFoto.lastIndexOf('.') + 1);
            var media = ext.equals("png") ? MediaType.IMAGE_PNG
                    : ext.equals("webp") ? MediaType.parseMediaType("image/webp") : MediaType.IMAGE_JPEG;
            return ResponseEntity.ok().contentType(media).body(new UrlResource(p.toUri()));
        } catch (Exception e) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Evidencia no disponible");
        }
    }
}
