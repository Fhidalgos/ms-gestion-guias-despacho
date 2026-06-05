package cl.duoc.ejemplo.microservicio.dto;

public class GuiaResponse {
    private String idGuia;
    private String transportista;
    private String fecha;
    private String archivoEfs;
    private String s3Key;
    private String mensaje;

    public GuiaResponse(String idGuia, String transportista, String fecha, String archivoEfs, String s3Key, String mensaje) {
        this.idGuia = idGuia;
        this.transportista = transportista;
        this.fecha = fecha;
        this.archivoEfs = archivoEfs;
        this.s3Key = s3Key;
        this.mensaje = mensaje;
    }

    public String getIdGuia() { return idGuia; }
    public String getTransportista() { return transportista; }
    public String getFecha() { return fecha; }
    public String getArchivoEfs() { return archivoEfs; }
    public String getS3Key() { return s3Key; }
    public String getMensaje() { return mensaje; }
}
