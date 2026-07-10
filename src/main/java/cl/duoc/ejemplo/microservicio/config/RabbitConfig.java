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

    // Cola principal donde se reciben las guías.
    public static final String COLA_PROCESAMIENTO =
            "guias.procesamiento.queue";

    // Exchange principal.
    public static final String EXCHANGE_GUIAS =
            "guias.exchange";

    // Clave utilizada para enviar las guías a la cola principal.
    public static final String ROUTING_KEY_PROCESAMIENTO =
            "guias.procesar";

    // Cola que almacenará los mensajes con errores.
    public static final String COLA_ERRORES =
            "guias.errores.queue";

    // Exchange encargado de recibir los mensajes fallidos.
    public static final String EXCHANGE_ERRORES =
            "guias.dlx";

    // Clave utilizada para enviar mensajes a la cola de errores.
    public static final String ROUTING_KEY_ERROR =
            "guias.error";

    /**
     * Cola principal.
     *
     * En caso de que un mensaje sea rechazado y no se vuelva
     * a colocar en la cola, RabbitMQ lo enviará al exchange
     * de errores.
     */
    @Bean
    public Queue colaProcesamiento() {

        return QueueBuilder
                .durable(COLA_PROCESAMIENTO)
                .withArgument(
                        "x-dead-letter-exchange",
                        EXCHANGE_ERRORES
                )
                .withArgument(
                        "x-dead-letter-routing-key",
                        ROUTING_KEY_ERROR
                )
                .build();
    }

    /**
     * Cola destinada a almacenar los mensajes que no pudieron
     * ser procesados correctamente.
     */
    @Bean
    public Queue colaErrores() {

        return QueueBuilder
                .durable(COLA_ERRORES)
                .build();
    }

    /**
     * Exchange principal de las guías.
     */
    @Bean
    public DirectExchange exchangeGuias() {

        return new DirectExchange(EXCHANGE_GUIAS);
    }

    /**
     * Dead Letter Exchange para mensajes con errores.
     */
    @Bean
    public DirectExchange exchangeErrores() {

        return new DirectExchange(EXCHANGE_ERRORES);
    }

    /**
     * Relaciona el exchange principal con la cola de procesamiento.
     */
    @Bean
    public Binding bindingProcesamiento(
            Queue colaProcesamiento,
            DirectExchange exchangeGuias
    ) {

        return BindingBuilder
                .bind(colaProcesamiento)
                .to(exchangeGuias)
                .with(ROUTING_KEY_PROCESAMIENTO);
    }

    /**
     * Relaciona el exchange de errores con la cola de errores.
     */
    @Bean
    public Binding bindingErrores(
            Queue colaErrores,
            DirectExchange exchangeErrores
    ) {

        return BindingBuilder
                .bind(colaErrores)
                .to(exchangeErrores)
                .with(ROUTING_KEY_ERROR);
    }
}