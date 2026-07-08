package cl.duoc.ejemplo.microservicio.services;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import cl.duoc.ejemplo.microservicio.config.RabbitConfig;

@Service
public class ProductorService {

    private final RabbitTemplate rabbitTemplate;

    public ProductorService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enviarMensaje(String mensaje) {

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_GUIAS,
                RabbitConfig.ROUTING_KEY_GUIAS,
                mensaje
        );

        System.out.println("Mensaje enviado a RabbitMQ: " + mensaje);
    }
}