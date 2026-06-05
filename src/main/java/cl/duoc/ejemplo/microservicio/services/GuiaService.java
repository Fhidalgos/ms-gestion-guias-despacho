package cl.duoc.ejemplo.microservicio.services;

import cl.duoc.ejemplo.microservicio.dto.GuiaRequest;
import cl.duoc.ejemplo.microservicio.dto.GuiaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

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

    public GuiaResponse crearGuia(GuiaRequest request) throws IOException {
        validarRequest(request);
        String id = "guia-" + UUID.randomUUID();
        String fecha = LocalDate.now().toString();
        String transportista = limpiar(request.getTransportista());
        Path carpeta = Path.of(efsPath, fecha, transportista);
        Files.createDirectories(carpeta);
        Path archivo = carpeta.resolve(id + ".pdf");
        Files.writeString(archivo, contenidoGuia(id, request), StandardCharsets.UTF_8);
        String key = construirKey(fecha, transportista, id);
        subirArchivoAS3(archivo, key, id, request.getUsuario());
        return new GuiaResponse(id, transportista, fecha, archivo.toString(), key,
                "Guía creada en EFS y subida automáticamente a S3");
    }

    public GuiaResponse subirGuiaExistente(String fecha, String transportista, String idGuia, String usuario) {
        transportista = limpiar(transportista);
        Path archivo = Path.of(efsPath, fecha, transportista, idGuia + ".pdf");
        if (!Files.exists(archivo)) {
            throw new NoSuchElementException("No existe la guía en EFS: " + archivo);
        }
        String key = construirKey(fecha, transportista, idGuia);
        subirArchivoAS3(archivo, key, idGuia, usuario);
        return new GuiaResponse(idGuia, transportista, fecha, archivo.toString(), key, "Guía subida a S3");
    }

    public byte[] descargarDesdeS3(String fecha, String transportista, String idGuia, String permiso) {
        if (!"DESCARGAR".equalsIgnoreCase(permiso)) {
            throw new SecurityException("Permiso inválido. Use header X-Permiso: DESCARGAR");
        }
        String key = construirKey(fecha, limpiar(transportista), idGuia);
        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(
                GetObjectRequest.builder().bucket(bucketName).key(key).build()
        );
        return objectBytes.asByteArray();
    }

    public GuiaResponse actualizarGuia(String fecha, String transportista, String idGuia, GuiaRequest request) throws IOException {
        validarRequest(request);
        transportista = limpiar(transportista);
        Path carpeta = Path.of(efsPath, fecha, transportista);
        Files.createDirectories(carpeta);
        Path archivo = carpeta.resolve(idGuia + ".pdf");
        Files.writeString(archivo, contenidoGuia(idGuia, request), StandardCharsets.UTF_8);
        String key = construirKey(fecha, transportista, idGuia);
        subirArchivoAS3(archivo, key, idGuia, request.getUsuario());
        return new GuiaResponse(idGuia, transportista, fecha, archivo.toString(), key, "Guía actualizada en EFS y S3");
    }

    public Map<String, String> eliminarGuia(String fecha, String transportista, String idGuia) throws IOException {
        transportista = limpiar(transportista);
        String key = construirKey(fecha, transportista, idGuia);
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
        Path archivo = Path.of(efsPath, fecha, transportista, idGuia + ".pdf");
        Files.deleteIfExists(archivo);
        return Map.of("mensaje", "Guía eliminada de S3 y EFS", "s3Key", key);
    }

    public List<Map<String, String>> consultarPorTransportistaYFecha(String transportista, String fecha) {
        String prefix = fecha + "/" + limpiar(transportista) + "/";
        ListObjectsV2Response listado = s3Client.listObjectsV2(
                ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix).build()
        );
        List<Map<String, String>> resultado = new ArrayList<>();
        for (S3Object obj : listado.contents()) {
            resultado.add(Map.of(
                    "s3Key", obj.key(),
                    "tamanoBytes", String.valueOf(obj.size()),
                    "ultimaModificacion", String.valueOf(obj.lastModified())
            ));
        }
        return resultado;
    }

    private void subirArchivoAS3(Path archivo, String key, String idGuia, String usuario) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("id-guia", idGuia);
        metadata.put("usuario", usuario == null ? "sin-usuario" : usuario);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/pdf")
                .metadata(metadata)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(archivo));
    }

    private String construirKey(String fecha, String transportista, String idGuia) {
        return fecha + "/" + transportista + "/" + idGuia + ".pdf";
    }

    private String contenidoGuia(String id, GuiaRequest request) {
        return "GUIA DE DESPACHO\n" +
                "ID: " + id + "\n" +
                "Fecha: " + LocalDate.now() + "\n" +
                "Transportista: " + request.getTransportista() + "\n" +
                "Destinatario: " + request.getDestinatario() + "\n" +
                "Dirección destino: " + request.getDireccionDestino() + "\n" +
                "Carga: " + request.getDescripcionCarga() + "\n" +
                "Usuario: " + request.getUsuario() + "\n";
    }

    private String limpiar(String texto) {
        if (texto == null || texto.isBlank()) return "sin-transportista";
        return texto.trim().replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private void validarRequest(GuiaRequest request) {
        if (request == null || request.getTransportista() == null || request.getTransportista().isBlank()) {
            throw new IllegalArgumentException("El transportista es obligatorio");
        }
    }
}
