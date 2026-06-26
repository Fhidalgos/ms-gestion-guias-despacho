package cl.duoc.ejemplo.microservicio.services;

import cl.duoc.ejemplo.microservicio.dto.GuiaRequest;
import cl.duoc.ejemplo.microservicio.dto.GuiaResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class GuiaService {

    private final S3Client s3Client;

    @Value("${app.efs.path}")
    private String efsPath;

    @Value("${app.s3.bucket}")
    private String bucketName;

    public GuiaService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Crea una guía de despacho.
     *
     * Primero genera el archivo en la ruta temporal de EFS
     * y después lo sube automáticamente al bucket de S3.
     */
    public GuiaResponse crearGuia(GuiaRequest request) throws IOException {

        validarRequest(request);

        String idGuia = "guia-" + UUID.randomUUID();
        String fecha = LocalDate.now().toString();
        String transportista = limpiar(request.getTransportista());

        Path carpeta = Path.of(
                efsPath,
                fecha,
                transportista
        );

        Files.createDirectories(carpeta);

        Path archivo = carpeta.resolve(idGuia + ".pdf");

        Files.writeString(
                archivo,
                contenidoGuia(idGuia, request),
                StandardCharsets.UTF_8
        );

        String key = construirKey(
                fecha,
                transportista,
                idGuia
        );

        subirArchivoAS3(
                archivo,
                key,
                idGuia,
                request.getUsuario()
        );

        return new GuiaResponse(
                idGuia,
                transportista,
                fecha,
                archivo.toString(),
                key,
                "Guía creada en EFS y subida automáticamente a S3"
        );
    }

    /**
     * Sube nuevamente a S3 una guía que ya existe en EFS.
     */
    public GuiaResponse subirGuiaExistente(
            String fecha,
            String transportista,
            String idGuia,
            String usuario
    ) {

        transportista = limpiar(transportista);

        Path archivo = Path.of(
                efsPath,
                fecha,
                transportista,
                idGuia + ".pdf"
        );

        if (!Files.exists(archivo)) {
            throw new NoSuchElementException(
                    "No existe la guía en EFS: " + archivo
            );
        }

        String key = construirKey(
                fecha,
                transportista,
                idGuia
        );

        subirArchivoAS3(
                archivo,
                key,
                idGuia,
                usuario
        );

        return new GuiaResponse(
                idGuia,
                transportista,
                fecha,
                archivo.toString(),
                key,
                "Guía subida a S3"
        );
    }

    /**
     * Descarga una guía desde AWS S3.
     *
     * Ya no valida el header X-Permiso. El permiso de descarga
     * se comprueba en SecurityConfig mediante el rol del JWT.
     */
    public byte[] descargarDesdeS3(
            String fecha,
            String transportista,
            String idGuia
    ) {

        String transportistaLimpio = limpiar(transportista);

        String key = construirKey(
                fecha,
                transportistaLimpio,
                idGuia
        );

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes =
                s3Client.getObjectAsBytes(getObjectRequest);

        return objectBytes.asByteArray();
    }

    /**
     * Modifica una guía existente.
     *
     * Actualiza el archivo temporal de EFS y reemplaza
     * el objeto correspondiente en AWS S3.
     */
    public GuiaResponse actualizarGuia(
            String fecha,
            String transportista,
            String idGuia,
            GuiaRequest request
    ) throws IOException {

        validarRequest(request);

        transportista = limpiar(transportista);

        Path carpeta = Path.of(
                efsPath,
                fecha,
                transportista
        );

        Files.createDirectories(carpeta);

        Path archivo = carpeta.resolve(idGuia + ".pdf");

        Files.writeString(
                archivo,
                contenidoGuia(idGuia, request),
                StandardCharsets.UTF_8
        );

        String key = construirKey(
                fecha,
                transportista,
                idGuia
        );

        subirArchivoAS3(
                archivo,
                key,
                idGuia,
                request.getUsuario()
        );

        return new GuiaResponse(
                idGuia,
                transportista,
                fecha,
                archivo.toString(),
                key,
                "Guía actualizada en EFS y S3"
        );
    }

    /**
     * Elimina una guía desde AWS S3 y desde EFS.
     */
    public Map<String, String> eliminarGuia(
            String fecha,
            String transportista,
            String idGuia
    ) throws IOException {

        transportista = limpiar(transportista);

        String key = construirKey(
                fecha,
                transportista,
                idGuia
        );

        DeleteObjectRequest deleteObjectRequest =
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

        s3Client.deleteObject(deleteObjectRequest);

        Path archivo = Path.of(
                efsPath,
                fecha,
                transportista,
                idGuia + ".pdf"
        );

        Files.deleteIfExists(archivo);

        return Map.of(
                "mensaje", "Guía eliminada de S3 y EFS",
                "s3Key", key
        );
    }

    /**
     * Consulta las guías almacenadas en S3
     * según transportista y fecha.
     */
    public List<Map<String, String>> consultarPorTransportistaYFecha(
            String transportista,
            String fecha
    ) {

        String transportistaLimpio = limpiar(transportista);

        String prefix = fecha
                + "/"
                + transportistaLimpio
                + "/";

        ListObjectsV2Request request =
                ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .build();

        ListObjectsV2Response listado =
                s3Client.listObjectsV2(request);

        List<Map<String, String>> resultado =
                new ArrayList<>();

        for (S3Object objeto : listado.contents()) {

            resultado.add(
                    Map.of(
                            "s3Key", objeto.key(),
                            "tamanoBytes",
                            String.valueOf(objeto.size()),
                            "ultimaModificacion",
                            String.valueOf(objeto.lastModified())
                    )
            );
        }

        return resultado;
    }

    /**
     * Sube un archivo físico al bucket S3,
     * agregando metadatos de la guía.
     */
    private void subirArchivoAS3(
            Path archivo,
            String key,
            String idGuia,
            String usuario
    ) {

        Map<String, String> metadata = new HashMap<>();

        metadata.put("id-guia", idGuia);
        metadata.put(
                "usuario",
                usuario == null || usuario.isBlank()
                        ? "sin-usuario"
                        : usuario
        );

        PutObjectRequest putObjectRequest =
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType("application/pdf")
                        .metadata(metadata)
                        .build();

        s3Client.putObject(
                putObjectRequest,
                RequestBody.fromFile(archivo)
        );
    }

    /**
     * Construye la ubicación del archivo dentro de S3.
     *
     * Ejemplo:
     * 2026-06-26/transportistaX/guia-123.pdf
     */
    private String construirKey(
            String fecha,
            String transportista,
            String idGuia
    ) {

        return fecha
                + "/"
                + transportista
                + "/"
                + idGuia
                + ".pdf";
    }

    /**
     * Genera el contenido del archivo de la guía.
     */
    private String contenidoGuia(
            String idGuia,
            GuiaRequest request
    ) {

        return "GUIA DE DESPACHO\n"
                + "----------------------------------\n"
                + "ID: " + idGuia + "\n"
                + "Fecha: " + LocalDate.now() + "\n"
                + "Transportista: "
                + request.getTransportista() + "\n"
                + "Destinatario: "
                + request.getDestinatario() + "\n"
                + "Dirección destino: "
                + request.getDireccionDestino() + "\n"
                + "Carga: "
                + request.getDescripcionCarga() + "\n"
                + "Usuario: "
                + request.getUsuario() + "\n"
                + "----------------------------------\n";
    }

    /**
     * Limpia el nombre del transportista para usarlo
     * de forma segura como carpeta de EFS y prefijo de S3.
     */
    private String limpiar(String texto) {

        if (texto == null || texto.isBlank()) {
            return "sin-transportista";
        }

        return texto
                .trim()
                .replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    /**
     * Valida los datos mínimos necesarios para crear
     * o actualizar una guía.
     */
    private void validarRequest(GuiaRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Los datos de la guía son obligatorios"
            );
        }

        if (request.getTransportista() == null
                || request.getTransportista().isBlank()) {

            throw new IllegalArgumentException(
                    "El transportista es obligatorio"
            );
        }
    }
}