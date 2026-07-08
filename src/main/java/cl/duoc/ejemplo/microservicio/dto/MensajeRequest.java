package cl.duoc.ejemplo.microservicio.dto;

public class MensajeRequest {

    private String mensaje;

    public MensajeRequest() {
    }

    public MensajeRequest(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}