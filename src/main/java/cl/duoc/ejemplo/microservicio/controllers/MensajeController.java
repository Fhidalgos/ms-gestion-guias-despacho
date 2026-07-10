package cl.duoc.ejemplo.microservicio.controllers;

import cl.duoc.ejemplo.microservicio.dto.MensajeRequest;
import cl.duoc.ejemplo.microservicio.entities.GuiaProcesada;
import cl.duoc.ejemplo.microservicio.services.ProcesamientoColaService;
import cl.duoc.ejemplo.microservicio.services.ProductorService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {

    private final ProductorService productorService;
    private final ProcesamientoColaService procesamientoColaService;

    public MensajeController(
            ProductorService productorService,
            ProcesamientoColaService procesamientoColaService
    ) {
        this.productorService = productorService;
        this.procesamientoColaService = procesamientoColaService;
    }

    /**
     * Envía un mensaje manualmente hacia RabbitMQ.
     */
    @PostMapping
    public ResponseEntity<String> enviarMensaje(
            @RequestBody MensajeRequest request
    ) {

        if (request.getMensaje() == null
                || request.getMensaje().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("El mensaje no puede estar vacío");
        }

        productorService.enviarMensaje(
                request.getMensaje()
        );

        return ResponseEntity.ok(
                "Mensaje enviado correctamente a RabbitMQ"
        );
    }

    /**
     * Consume el siguiente mensaje disponible en la cola 1
     * y guarda sus datos en Oracle Cloud.
     */
    @PostMapping("/procesar")
    public ResponseEntity<?> procesarMensaje() {

        try {

            GuiaProcesada guiaProcesada =
                    procesamientoColaService
                            .procesarSiguienteMensaje();

            return ResponseEntity.ok(
                    Map.of(
                            "mensaje",
                            "Guía procesada y guardada en Oracle Cloud",
                            "id",
                            guiaProcesada.getId(),
                            "idGuia",
                            guiaProcesada.getIdGuia(),
                            "transportista",
                            guiaProcesada.getTransportista()
                    )
            );

        } catch (NoSuchElementException ex) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    ex.getMessage()
                            )
                    );

        } catch (IllegalStateException ex) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "error",
                                    ex.getMessage()
                            )
                    );
        }
    }
}