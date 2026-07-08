package cl.duoc.ejemplo.microservicio.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String COLA_GUIAS = "guias.queue";
    public static final String EXCHANGE_GUIAS = "guias.exchange";
    public static final String ROUTING_KEY_GUIAS = "guias.creada";

    @Bean
    public Queue guiasQueue() {
        return QueueBuilder
                .durable(COLA_GUIAS)
                .build();
    }

    @Bean
    public DirectExchange guiasExchange() {
        return new DirectExchange(EXCHANGE_GUIAS);
    }

    @Bean
    public Binding guiasBinding(
            Queue guiasQueue,
            DirectExchange guiasExchange) {

        return BindingBuilder
                .bind(guiasQueue)
                .to(guiasExchange)
                .with(ROUTING_KEY_GUIAS);
    }
}