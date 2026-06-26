package cl.duoc.ejemplo.microservicio.controllers;

import cl.duoc.ejemplo.microservicio.dto.GuiaRequest;
import cl.duoc.ejemplo.microservicio.dto.GuiaResponse;
import cl.duoc.ejemplo.microservicio.services.GuiaService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/guias")
public class GuiaController {

    private final GuiaService guiaService;

    public GuiaController(GuiaService guiaService) {
        this.guiaService = guiaService;
    }

    /**
     * Crea una nueva guía de despacho.
     * La guía se almacena en EFS y se sube automáticamente a S3.
     */
    @PostMapping
    public ResponseEntity<GuiaResponse> crearGuia(
            @RequestBody GuiaRequest request
    ) throws IOException {

        GuiaResponse respuesta = guiaService.crearGuia(request);

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Sube nuevamente a S3 una guía que ya existe en EFS.
     */
    @PostMapping("/{idGuia}/s3")
    public ResponseEntity<GuiaResponse> subirGuiaAS3(
            @PathVariable String idGuia,
            @RequestParam String fecha,
            @RequestParam String transportista,
            @RequestParam(defaultValue = "sistema") String usuario
    ) {

        GuiaResponse respuesta = guiaService.subirGuiaExistente(
                fecha,
                transportista,
                idGuia,
                usuario
        );

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Descarga una guía desde AWS S3.
     *
     * Ya no utiliza el header X-Permiso.
     * El acceso se controla mediante el rol del JWT en SecurityConfig.
     */
    @GetMapping("/{idGuia}/descargar")
    public ResponseEntity<byte[]> descargarGuia(
            @PathVariable String idGuia,
            @RequestParam String fecha,
            @RequestParam String transportista
    ) {

        byte[] archivo = guiaService.descargarDesdeS3(
                fecha,
                transportista,
                idGuia
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + idGuia + ".pdf\""
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(archivo);
    }

    /**
     * Modifica una guía existente y actualiza el archivo
     * tanto en EFS como en AWS S3.
     */
    @PutMapping("/{idGuia}")
    public ResponseEntity<GuiaResponse> actualizarGuia(
            @PathVariable String idGuia,
            @RequestParam String fecha,
            @RequestParam String transportista,
            @RequestBody GuiaRequest request
    ) throws IOException {

        GuiaResponse respuesta = guiaService.actualizarGuia(
                fecha,
                transportista,
                idGuia,
                request
        );

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Elimina una guía específica desde S3 y EFS.
     */
    @DeleteMapping("/{idGuia}")
    public ResponseEntity<Map<String, String>> eliminarGuia(
            @PathVariable String idGuia,
            @RequestParam String fecha,
            @RequestParam String transportista
    ) throws IOException {

        Map<String, String> respuesta = guiaService.eliminarGuia(
                fecha,
                transportista,
                idGuia
        );

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Consulta las guías almacenadas según transportista y fecha.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, String>>> consultarGuias(
            @RequestParam String transportista,
            @RequestParam String fecha
    ) {

        List<Map<String, String>> resultado =
                guiaService.consultarPorTransportistaYFecha(
                        transportista,
                        fecha
                );

        return ResponseEntity.ok(resultado);
    }

    /**
     * Maneja errores de datos inválidos o recursos inexistentes.
     *
     * Los errores 401 y 403 de seguridad son administrados
     * directamente por Spring Security.
     */
    @ExceptionHandler({
            IllegalArgumentException.class,
            NoSuchElementException.class
    })
    public ResponseEntity<Map<String, String>> manejarErrores(
            RuntimeException ex
    ) {

        return ResponseEntity.badRequest()
                .body(Map.of("error", ex.getMessage()));
    }
}