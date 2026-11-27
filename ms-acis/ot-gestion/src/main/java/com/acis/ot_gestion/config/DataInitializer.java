package com.acis.ot_gestion.config;
import com.acis.ot_gestion.model.*;import com.acis.ot_gestion.repository.OrdenTrabajoRepository;import org.springframework.boot.CommandLineRunner;import org.springframework.context.annotation.*;
@Configuration public class DataInitializer {
 @Bean CommandLineRunner seed(OrdenTrabajoRepository repo){return args->{if(repo.count()>0)return; for(int i=1;i<=7;i++){var o=new OrdenTrabajo();o.setCodigoOt("OT-"+(1000+i));o.setClienteCorreo(i%2==0?"cliente2@acis.cl":"cliente@acis.cl");o.setLocal("Local demo "+i);o.setEquipo("Equipo HVAC "+i);o.setDescripcion("Mantención de demostración");o.setFechaProgramada("2026-12-"+String.format("%02d",i)+" 09:00");o.setTipo(i%2==0?TipoOt.CORRECTIVA:TipoOt.PREVENTIVA);o.setPrioridad(i%3==0?PrioridadOt.ALTA:PrioridadOt.MEDIA);o.setEstado(EstadoOt.CREADA);repo.save(o);}};}
}
