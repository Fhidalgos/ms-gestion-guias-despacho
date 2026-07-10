package cl.duoc.ejemplo.microservicio.services;

import cl.duoc.ejemplo.microservicio.config.RabbitConfig;
import cl.duoc.ejemplo.microservicio.dto.GuiaMensaje;
import cl.duoc.ejemplo.microservicio.entities.GuiaProcesada;
import cl.duoc.ejemplo.microservicio.repositories.GuiaProcesadaRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ProcesamientoColaService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final GuiaProcesadaRepository guiaProcesadaRepository;
    private final ProductorService productorService;

    public ProcesamientoColaService(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            GuiaProcesadaRepository guiaProcesadaRepository,
            ProductorService productorService
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.guiaProcesadaRepository = guiaProcesadaRepository;
        this.productorService = productorService;
    }

    /**
     * Obtiene el siguiente mensaje disponible en la cola principal,
     * convierte el JSON y guarda la guía en Oracle Cloud.
     */
    @Transactional
    public GuiaProcesada procesarSiguienteMensaje() {

        Object mensajeRecibido = rabbitTemplate.receiveAndConvert(
                RabbitConfig.COLA_PROCESAMIENTO
        );

        if (mensajeRecibido == null) {
            throw new NoSuchElementException(
                    "No hay mensajes disponibles en la cola de procesamiento"
            );
        }

        String mensajeJson = String.valueOf(mensajeRecibido);

        try {

            GuiaMensaje guiaMensaje = objectMapper.readValue(
                    mensajeJson,
                    GuiaMensaje.class
            );

            /*
             * Se verifica si la guía ya fue procesada.
             * Esto evita guardar registros duplicados.
             */
            Optional<GuiaProcesada> guiaExistente =
                    guiaProcesadaRepository.findByIdGuia(
                            guiaMensaje.getIdGuia()
                    );

            if (guiaExistente.isPresent()) {

                System.out.println(
                        "La guía ya estaba registrada en Oracle: "
                                + guiaMensaje.getIdGuia()
                );

                return guiaExistente.get();
            }

            GuiaProcesada guiaProcesada = new GuiaProcesada(
                    guiaMensaje.getIdGuia(),
                    guiaMensaje.getTransportista(),
                    guiaMensaje.getFecha(),
                    guiaMensaje.getDestinatario(),
                    guiaMensaje.getDireccionDestino(),
                    guiaMensaje.getDescripcionCarga(),
                    guiaMensaje.getUsuario()
            );

            GuiaProcesada guiaGuardada =
                    guiaProcesadaRepository.save(guiaProcesada);

            System.out.println(
                    "Guía guardada correctamente en Oracle Cloud: "
                            + guiaGuardada.getIdGuia()
            );

            return guiaGuardada;

        } catch (Exception ex) {

            String mensajeError =
                    "Error al procesar mensaje de la cola. "
                            + "Mensaje original: "
                            + mensajeJson
                            + ". Detalle: "
                            + ex.getMessage();

            productorService.enviarMensajeError(mensajeError);

            System.out.println(
                    "El mensaje fue enviado a la cola de errores: "
                            + mensajeError
            );

            throw new IllegalStateException(
                    "No se pudo procesar el mensaje de la cola",
                    ex
            );
        }
    }
}