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

@RestController
@RequestMapping("/api/guias")
public class GuiaController {

    private final GuiaService guiaService;

    public GuiaController(GuiaService guiaService) {
        this.guiaService = guiaService;
    }

    @PostMapping
    public ResponseEntity<GuiaResponse> crearGuia(@RequestBody GuiaRequest request) throws IOException {
        return ResponseEntity.ok(guiaService.crearGuia(request));
    }

    @PostMapping("/{idGuia}/s3")
    public ResponseEntity<GuiaResponse> subirGuiaAS3(@PathVariable String idGuia,
                                                     @RequestParam String fecha,
                                                     @RequestParam String transportista,
                                                     @RequestParam(defaultValue = "sistema") String usuario) {
        return ResponseEntity.ok(guiaService.subirGuiaExistente(fecha, transportista, idGuia, usuario));
    }

    @GetMapping("/{idGuia}/descargar")
    public ResponseEntity<byte[]> descargarGuia(@PathVariable String idGuia,
                                                @RequestParam String fecha,
                                                @RequestParam String transportista,
                                                @RequestHeader(value = "X-Permiso", required = false) String permiso) {
        byte[] archivo = guiaService.descargarDesdeS3(fecha, transportista, idGuia, permiso);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + idGuia + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(archivo);
    }

    @PutMapping("/{idGuia}")
    public ResponseEntity<GuiaResponse> actualizarGuia(@PathVariable String idGuia,
                                                       @RequestParam String fecha,
                                                       @RequestParam String transportista,
                                                       @RequestBody GuiaRequest request) throws IOException {
        return ResponseEntity.ok(guiaService.actualizarGuia(fecha, transportista, idGuia, request));
    }

    @DeleteMapping("/{idGuia}")
    public ResponseEntity<Map<String, String>> eliminarGuia(@PathVariable String idGuia,
                                                            @RequestParam String fecha,
                                                            @RequestParam String transportista) throws IOException {
        return ResponseEntity.ok(guiaService.eliminarGuia(fecha, transportista, idGuia));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> consultarGuias(@RequestParam String transportista,
                                                                    @RequestParam String fecha) {
        return ResponseEntity.ok(guiaService.consultarPorTransportistaYFecha(transportista, fecha));
    }

    @ExceptionHandler({IllegalArgumentException.class, SecurityException.class})
    public ResponseEntity<Map<String, String>> manejarErrores(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
