package cl.duoc.ejemplo.microservicio.services;

import cl.duoc.ejemplo.microservicio.config.RabbitConfig;
import cl.duoc.ejemplo.microservicio.dto.GuiaMensaje;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProductorService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public ProductorService(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Envía los datos completos de una guía hacia la cola 1.
     */
    public void enviarGuia(GuiaMensaje guiaMensaje) {

        try {
            String guiaJson =
                    objectMapper.writeValueAsString(guiaMensaje);

            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE_GUIAS,
                    RabbitConfig.ROUTING_KEY_PROCESAMIENTO,
                    guiaJson
            );

            System.out.println(
                    "Guía enviada a la cola de procesamiento: "
                            + guiaJson
            );

        } catch (JsonProcessingException e) {

            enviarMensajeError(
                    "No se pudo convertir la guía a JSON. "
                            + e.getMessage()
            );
        }
    }

    /**
     * Mantiene funcionando el endpoint creado durante la semana 7.
     */
    public void enviarMensaje(String mensaje) {

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_GUIAS,
                RabbitConfig.ROUTING_KEY_PROCESAMIENTO,
                mensaje
        );

        System.out.println(
                "Mensaje enviado a la cola de procesamiento: "
                        + mensaje
        );
    }

    /**
     * Envía los mensajes fallidos directamente a la cola 2.
     */
    public void enviarMensajeError(String mensajeError) {

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_ERRORES,
                RabbitConfig.ROUTING_KEY_ERROR,
                mensajeError
        );

        System.out.println(
                "Mensaje enviado a la cola de errores: "
                        + mensajeError
        );
    }
}