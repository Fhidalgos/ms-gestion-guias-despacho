package cl.duoc.ejemplo.microservicio.controllers;

import cl.duoc.ejemplo.microservicio.dto.MensajeRequest;
import cl.duoc.ejemplo.microservicio.services.ProductorService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {

    private final ProductorService productorService;

    public MensajeController(ProductorService productorService) {
        this.productorService = productorService;
    }

    @PostMapping
    public ResponseEntity<String> enviarMensaje(
            @RequestBody MensajeRequest request) {

        if (request.getMensaje() == null
                || request.getMensaje().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("El mensaje no puede estar vacío");
        }

        productorService.enviarMensaje(request.getMensaje());

        return ResponseEntity.ok(
                "Mensaje enviado correctamente a RabbitMQ"
        );
    }
}