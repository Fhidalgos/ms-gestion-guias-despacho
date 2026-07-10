package cl.duoc.ejemplo.microservicio.dto;

public class GuiaMensaje {

    private String idGuia;
    private String transportista;
    private String fecha;
    private String destinatario;
    private String direccionDestino;
    private String descripcionCarga;
    private String usuario;

    public GuiaMensaje() {
    }

    public GuiaMensaje(
            String idGuia,
            String transportista,
            String fecha,
            String destinatario,
            String direccionDestino,
            String descripcionCarga,
            String usuario
    ) {
        this.idGuia = idGuia;
        this.transportista = transportista;
        this.fecha = fecha;
        this.destinatario = destinatario;
        this.direccionDestino = direccionDestino;
        this.descripcionCarga = descripcionCarga;
        this.usuario = usuario;
    }

    public String getIdGuia() {
        return idGuia;
    }

    public void setIdGuia(String idGuia) {
        this.idGuia = idGuia;
    }

    public String getTransportista() {
        return transportista;
    }

    public void setTransportista(String transportista) {
        this.transportista = transportista;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getDireccionDestino() {
        return direccionDestino;
    }

    public void setDireccionDestino(String direccionDestino) {
        this.direccionDestino = direccionDestino;
    }

    public String getDescripcionCarga() {
        return descripcionCarga;
    }

    public void setDescripcionCarga(String descripcionCarga) {
        this.descripcionCarga = descripcionCarga;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}