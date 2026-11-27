package com.acis.ot_gestion.controller;
import com.acis.ot_gestion.dto.OrdenRequest;
import com.acis.ot_gestion.dto.IntervencionUpdateRequest;
import com.acis.ot_gestion.model.*;
import com.acis.ot_gestion.service.OrdenTrabajoService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/ordenes")
public class OrdenTrabajoController {
 private final OrdenTrabajoService service; public OrdenTrabajoController(OrdenTrabajoService s){service=s;}
 @GetMapping public Object listar(@RequestParam(required=false) EstadoOt estado,Authentication a){return service.listar(a.getName(),rol(a),estado);}
 @GetMapping("/{codigoOt}") public Object buscar(@PathVariable String codigoOt,Authentication a){return service.buscar(codigoOt,a.getName(),rol(a));}
 @PostMapping @PreAuthorize("hasAnyRole('ADMINISTRADOR','PLANIFICADOR')") public ResponseEntity<?> crear(@Valid @RequestBody OrdenRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));}
 @PutMapping("/{codigoOt}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','PLANIFICADOR')") public Object actualizar(@PathVariable String codigoOt,@Valid @RequestBody OrdenRequest r){return service.actualizar(codigoOt,r);}
 @PutMapping("/{codigoOt}/asignacion") @PreAuthorize("hasAnyRole('ADMINISTRADOR','PLANIFICADOR')") public Object asignar(@PathVariable String codigoOt,@RequestBody Map<String,String> body){return service.asignar(codigoOt,body.get("tecnicoAsignado"));}
 @PatchMapping("/{codigoOt}/estado") public Object estado(@PathVariable String codigoOt,@RequestBody Map<String,String> body,Authentication a){return service.cambiarEstado(codigoOt,EstadoOt.valueOf(body.getOrDefault("estado","")),a.getName(),rol(a),body.get("observacionCliente"),body.get("respuestaTecnico"));}
 @PutMapping("/{codigoOt}/intervencion") @PreAuthorize("hasRole('TECNICO')") public Object actualizarIntervencion(@PathVariable String codigoOt,@RequestBody IntervencionUpdateRequest body,Authentication a){return service.actualizarIntervencion(codigoOt,a.getName(),rol(a),body);}
 @DeleteMapping("/{codigoOt}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','PLANIFICADOR')") public ResponseEntity<Void> borrar(@PathVariable String codigoOt){service.borrar(codigoOt);return ResponseEntity.noContent().build();}
 private String rol(Authentication a){return a.getAuthorities().stream().findFirst().map(x->x.getAuthority().replace("ROLE_","")).orElse("");}
}
