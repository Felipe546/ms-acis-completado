package com.acis.ot_gestion.model;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="ordenes_trabajo", uniqueConstraints=@UniqueConstraint(columnNames="codigo_ot"))
public class OrdenTrabajo {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="codigo_ot",nullable=false,unique=true) private String codigoOt;
 @Column(nullable=false) private String clienteCorreo;
 @Column(nullable=false) private String local;
 @Column(nullable=false) private String equipo;
 @Column(nullable=false,length=2000) private String descripcion;
 @Column(nullable=false) private String fechaProgramada;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private TipoOt tipo;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private PrioridadOt prioridad;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private EstadoOt estado=EstadoOt.CREADA;
 private String tecnicoAsignado;
 private String checkIn, ubicacionCheckIn, checkOut;
 @Column(length=3000) private String diagnostico, actividad, materiales, observacionCliente, respuestaTecnico;
 private String fechaConformidad, fotoEvidencia;
 private Long ultimaActualizacion=System.currentTimeMillis();
 @PrePersist @PreUpdate void touch(){ultimaActualizacion=Instant.now().toEpochMilli();}
 public Long getId(){return id;} public String getCodigoOt(){return codigoOt;} public void setCodigoOt(String v){codigoOt=v;}
 public String getClienteCorreo(){return clienteCorreo;} public void setClienteCorreo(String v){clienteCorreo=v;}
 public String getLocal(){return local;} public void setLocal(String v){local=v;} public String getEquipo(){return equipo;} public void setEquipo(String v){equipo=v;}
 public String getDescripcion(){return descripcion;} public void setDescripcion(String v){descripcion=v;} public String getFechaProgramada(){return fechaProgramada;} public void setFechaProgramada(String v){fechaProgramada=v;}
 public TipoOt getTipo(){return tipo;} public void setTipo(TipoOt v){tipo=v;} public PrioridadOt getPrioridad(){return prioridad;} public void setPrioridad(PrioridadOt v){prioridad=v;}
 public EstadoOt getEstado(){return estado;} public void setEstado(EstadoOt v){estado=v;} public String getTecnicoAsignado(){return tecnicoAsignado;} public void setTecnicoAsignado(String v){tecnicoAsignado=v;}
 public String getCheckIn(){return checkIn;} public void setCheckIn(String v){checkIn=v;} public String getUbicacionCheckIn(){return ubicacionCheckIn;} public void setUbicacionCheckIn(String v){ubicacionCheckIn=v;}
 public String getCheckOut(){return checkOut;} public void setCheckOut(String v){checkOut=v;} public String getDiagnostico(){return diagnostico;} public void setDiagnostico(String v){diagnostico=v;}
 public String getActividad(){return actividad;} public void setActividad(String v){actividad=v;} public String getMateriales(){return materiales;} public void setMateriales(String v){materiales=v;}
 public String getObservacionCliente(){return observacionCliente;} public void setObservacionCliente(String v){observacionCliente=v;} public String getRespuestaTecnico(){return respuestaTecnico;} public void setRespuestaTecnico(String v){respuestaTecnico=v;}
 public String getFechaConformidad(){return fechaConformidad;} public void setFechaConformidad(String v){fechaConformidad=v;} public String getFotoEvidencia(){return fotoEvidencia;} public void setFotoEvidencia(String v){fotoEvidencia=v;}
 public Long getUltimaActualizacion(){return ultimaActualizacion;}
}
