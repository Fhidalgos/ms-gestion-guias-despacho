package cl.duoc.ejemplo.microservicio.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "GUIA_COLA_PROCESADA")
public class GuiaProcesada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(
            name = "ID_GUIA",
            nullable = false,
            length = 100
    )
    private String idGuia;

    @Column(
            name = "TRANSPORTISTA",
            nullable = false,
            length = 100
    )
    private String transportista;

    @Column(
            name = "FECHA_GUIA",
            nullable = false,
            length = 10
    )
    private String fechaGuia;

    @Column(
            name = "DESTINATARIO",
            length = 150
    )
    private String destinatario;

    @Column(
            name = "DIRECCION_DESTINO",
            length = 250
    )
    private String direccionDestino;

    @Column(
            name = "DESCRIPCION_CARGA",
            length = 500
    )
    private String descripcionCarga;

    @Column(
            name = "USUARIO",
            length = 100
    )
    private String usuario;

    @Column(
            name = "FECHA_PROCESAMIENTO",
            nullable = false
    )
    private LocalDateTime fechaProcesamiento;

    public GuiaProcesada() {
    }

    public GuiaProcesada(
            String idGuia,
            String transportista,
            String fechaGuia,
            String destinatario,
            String direccionDestino,
            String descripcionCarga,
            String usuario
    ) {
        this.idGuia = idGuia;
        this.transportista = transportista;
        this.fechaGuia = fechaGuia;
        this.destinatario = destinatario;
        this.direccionDestino = direccionDestino;
        this.descripcionCarga = descripcionCarga;
        this.usuario = usuario;
    }

    /**
     * Asigna automáticamente la fecha y hora
     * antes de insertar el registro en Oracle.
     */
    @PrePersist
    public void asignarFechaProcesamiento() {

        if (fechaProcesamiento == null) {
            fechaProcesamiento = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getFechaGuia() {
        return fechaGuia;
    }

    public void setFechaGuia(String fechaGuia) {
        this.fechaGuia = fechaGuia;
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

    public LocalDateTime getFechaProcesamiento() {
        return fechaProcesamiento;
    }

    public void setFechaProcesamiento(
            LocalDateTime fechaProcesamiento
    ) {
        this.fechaProcesamiento = fechaProcesamiento;
    }
}
