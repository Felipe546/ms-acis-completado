package com.acis.ot_gestion.service;

import com.acis.ot_gestion.model.EstadoOt;
import com.acis.ot_gestion.model.OrdenTrabajo;
import com.acis.ot_gestion.repository.OrdenTrabajoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
class OrdenTrabajoServiceTest {
    private OrdenTrabajoRepository repository;
    private OrdenTrabajoService service;
    private OrdenTrabajo orden;
    private AtomicInteger guardados;

    @BeforeEach
    void prepararOrdenDemo() {
        orden = new OrdenTrabajo();
        orden.setCodigoOt("OT-1007");
        orden.setClienteCorreo("cliente@acis.cl");
        orden.setTecnicoAsignado("am.barria@duocuc.cl");
        orden.setEstado(EstadoOt.CREADA);
        guardados = new AtomicInteger();
        repository = (OrdenTrabajoRepository) Proxy.newProxyInstance(
                OrdenTrabajoRepository.class.getClassLoader(),
                new Class<?>[]{OrdenTrabajoRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByCodigoOt" -> Optional.of(orden);
                    case "save" -> {
                        guardados.incrementAndGet();
                        yield args[0];
                    }
                    default -> null;
                });
        service = new OrdenTrabajoService(repository);
    }

    @Test
    void tecnicoAsignadoPuedeAceptarOrdenDemoAunqueSigaCreada() {
        OrdenTrabajo resultado = service.cambiarEstado(
                "OT-1007", EstadoOt.ACEPTADA, "am.barria@duocuc.cl", "TECNICO", null, null);

        assertEquals(EstadoOt.ACEPTADA, resultado.getEstado());
    }

    @Test
    void otroTecnicoNoPuedeAceptarLaOrden() {
        assertThrows(ResponseStatusException.class, () -> service.cambiarEstado(
                "OT-1007", EstadoOt.ACEPTADA, "otro@acis.cl", "TECNICO", null, null));

        assertEquals(0, guardados.get());
    }
}
