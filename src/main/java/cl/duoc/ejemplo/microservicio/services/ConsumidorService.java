package cl.duoc.ejemplo.microservicio.services;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import cl.duoc.ejemplo.microservicio.config.RabbitConfig;

@Service
public class ConsumidorService {

    /**
     * Escucha permanentemente los mensajes que llegan
     * a la cola guias.queue.
     */
    @RabbitListener(queues = RabbitConfig.COLA_GUIAS)
    public void recibirMensaje(String mensaje) {

        System.out.println(
                "Mensaje recibido desde RabbitMQ: " + mensaje
        );
    }
}
