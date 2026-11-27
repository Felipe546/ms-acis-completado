package com.acis.ot_gestion.repository;
import com.acis.ot_gestion.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo,Long> {
 Optional<OrdenTrabajo> findByCodigoOt(String codigoOt); boolean existsByCodigoOt(String codigoOt);
 List<OrdenTrabajo> findByClienteCorreoIgnoreCase(String correo); List<OrdenTrabajo> findByTecnicoAsignadoIgnoreCase(String correo);
 List<OrdenTrabajo> findByEstado(EstadoOt estado);
}
