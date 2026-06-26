package cl.duoc.ejemplo.microservicio.dto;

public class GuiaRequest {
    private String transportista;
    private String destinatario;
    private String direccionDestino;
    private String descripcionCarga;
    private String usuario;

    public String getTransportista() { return transportista; }
    public void setTransportista(String transportista) { this.transportista = transportista; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public String getDireccionDestino() { return direccionDestino; }
    public void setDireccionDestino(String direccionDestino) { this.direccionDestino = direccionDestino; }
    public String getDescripcionCarga() { return descripcionCarga; }
    public void setDescripcionCarga(String descripcionCarga) { this.descripcionCarga = descripcionCarga; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
}
