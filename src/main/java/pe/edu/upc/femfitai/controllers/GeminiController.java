package pe.edu.upc.femfitai.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.femfitai.services.implementations.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientResponseException;
import java.util.Map;

@RestController
@RequestMapping("/ia")
public class GeminiController {

    private final GeminiService geminiService;
    private static final Logger log =
            LoggerFactory.getLogger(GeminiController.class);
    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/probar")
    public ResponseEntity<?> probarGemini(
            @RequestBody Map<String, String> request) {

        String mensaje = request.get("mensaje");

        if (mensaje == null || mensaje.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message", "Debes ingresar un mensaje"
                    ));
        }

        try {
            String respuesta = geminiService.responder(mensaje);

            return ResponseEntity.ok(
                    Map.of("respuesta", respuesta)
            );

        }catch (RestClientResponseException e) {

            log.error("Error HTTP de Gemini: status={}, body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());

            return ResponseEntity.status(502)
                    .body(Map.of(
                            "message", "Gemini rechazó la solicitud"
                    ));

        } catch (Exception e) {

            log.error("Error inesperado al comunicarse con Gemini", e);

            return ResponseEntity.status(503)
                    .body(Map.of(
                            "message", "No fue posible comunicarse con Gemini"
                    ));
        }
    }
}