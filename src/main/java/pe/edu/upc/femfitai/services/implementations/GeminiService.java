package pe.edu.upc.femfitai.services.implementations;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import pe.edu.upc.femfitai.dtos.MensajeChatDTO;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;


@Service
public class GeminiService {

    private final RestClient restClient;
    private final String model;
    private static final Logger log =
            LoggerFactory.getLogger(GeminiService.class);

    private static final String SYSTEM_INSTRUCTION = """
        Eres FemFitAI, un asistente de bienestar femenino,
        entrenamiento y actividad física.

        Responde siempre en español, de manera amigable,
        clara y respetuosa.

        Ofrece orientación general sobre entrenamiento,
        ejercicio y bienestar.

        No diagnostiques enfermedades, no recomiendes
        medicamentos ni reemplaces a profesionales de salud.

        Si una usuaria presenta síntomas preocupantes,
        recomiéndale consultar con un profesional.
        
            Responde en español, de manera amigable y concisa.
            Utiliza normalmente entre 2 y 5 oraciones.
            Evita explicaciones innecesariamente largas.
        """;


    public GeminiService(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model) {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(Duration.ofSeconds(45));

        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .requestFactory(requestFactory)
                .defaultHeader("x-goog-api-key", apiKey)
                .build();

        this.model = model;
    }

    public String responder(String mensaje) {

        if (mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException(
                    "El mensaje no puede estar vacío");
        }

        Map<String, Object> body = Map.of(
                "systemInstruction",
                Map.of(
                        "parts",
                        List.of(Map.of("text", SYSTEM_INSTRUCTION))
                ),

                "contents",
                List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(
                                        Map.of("text", mensaje)
                                )
                        )
                ),

                "generationConfig",
                Map.of(
                        "maxOutputTokens", 1024,
                        "thinkingConfig", Map.of(
                                "thinkingLevel", "LOW"
                        )
                )
        );


        Map<?, ?> response = restClient.post()
                .uri("/models/{model}:generateContent", model)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException(
                    "Gemini no devolvió una respuesta");
        }

// Obtener candidates
        Object candidatesObject = response.get("candidates");

        if (!(candidatesObject instanceof List<?> candidates)
                || candidates.isEmpty()) {
            throw new IllegalStateException(
                    "Gemini no generó candidatos");
        }

// Obtener el primer candidato
        Object candidateObject = candidates.get(0);

        if (!(candidateObject instanceof Map<?, ?> candidate)) {
            throw new IllegalStateException(
                    "Formato de respuesta inválido");
        }

// Obtener content
        Object contentObject = candidate.get("content");

        if (!(contentObject instanceof Map<?, ?> content)) {
            throw new IllegalStateException(
                    "Gemini no devolvió contenido");
        }

// Obtener parts
        Object partsObject = content.get("parts");

        if (!(partsObject instanceof List<?> parts)) {
            throw new IllegalStateException(
                    "Gemini no devolvió partes de texto");
        }

// Extraer el texto
        StringBuilder respuesta = new StringBuilder();

        for (Object partObject : parts) {

            if (partObject instanceof Map<?, ?> part) {

                Object textObject = part.get("text");

                if (textObject instanceof String text) {
                    respuesta.append(text);
                }
            }
        }

        if (respuesta.isEmpty()) {
            throw new IllegalStateException(
                    "Gemini no generó texto");
        }

        return respuesta.toString();

    }


    private String enviarPeticionGemini(Map<String, Object> body) {

        // Declarar la respuesta antes del try
        Map<?, ?> response = null;

        long inicio = System.nanoTime();

        try {

            response = restClient.post()
                    .uri("/models/{model}:generateContent", model)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

        } finally {

            long tiempoMs =
                    (System.nanoTime() - inicio) / 1_000_000;

            log.info("Tiempo de respuesta Gemini: {} ms", tiempoMs);
        }

        if (response == null) {
            throw new IllegalStateException(
                    "Gemini no devolvió una respuesta");
        }

        // Obtener los candidatos
        Object candidatesObject = response.get("candidates");

        if (!(candidatesObject instanceof List<?> candidates)
                || candidates.isEmpty()) {
            throw new IllegalStateException(
                    "Gemini no generó candidatos");
        }

        // Obtener el primer candidato
        if (!(candidates.get(0) instanceof Map<?, ?> candidate)) {
            throw new IllegalStateException(
                    "Formato de respuesta inválido");
        }

        // Obtener contenido
        if (!(candidate.get("content") instanceof Map<?, ?> content)) {
            throw new IllegalStateException(
                    "Gemini no devolvió contenido");
        }

        // Obtener partes del mensaje
        if (!(content.get("parts") instanceof List<?> parts)) {
            throw new IllegalStateException(
                    "Gemini no devolvió partes de texto");
        }

        StringBuilder respuesta = new StringBuilder();

        for (Object partObject : parts) {

            if (partObject instanceof Map<?, ?> part) {

                Object textObject = part.get("text");

                if (textObject instanceof String text) {
                    respuesta.append(text);
                }
            }
        }

        if (respuesta.isEmpty()) {
            throw new IllegalStateException(
                    "Gemini no generó texto");
        }

        return respuesta.toString();
    }


    public String conversar(
            String mensaje,
            List<MensajeChatDTO> historial) {

        if (mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException(
                    "El mensaje no puede estar vacío");
        }

        List<Map<String, Object>> contents = new ArrayList<>();

        // Conservar solamente las últimas 8 interacciones
        List<MensajeChatDTO> recientes =
                historial == null
                        ? List.of()
                        : historial.subList(
                        Math.max(0, historial.size() - 8),
                        historial.size()
                );

        String rolAnterior = null;

        for (MensajeChatDTO m : recientes) {

            if (m == null || m.role() == null
                    || m.content() == null
                    || m.content().isBlank()) {
                throw new IllegalArgumentException(
                        "El historial contiene mensajes inválidos");
            }

            if (!m.role().equals("user")
                    && !m.role().equals("model")) {
                throw new IllegalArgumentException(
                        "Rol de mensaje no permitido");
            }

            if (m.role().equals(rolAnterior)) {
                throw new IllegalArgumentException(
                        "Los mensajes deben alternar entre usuario e IA");
            }

            if (contents.isEmpty() && !m.role().equals("user")) {
                throw new IllegalArgumentException(
                        "El historial debe comenzar con un mensaje de usuario");
            }

            rolAnterior = m.role();

            contents.add(Map.of(
                    "role", m.role(),
                    "parts", List.of(
                            Map.of("text", m.content())
                    )
            ));
        }

        // El historial debe terminar con una respuesta de la IA
        if (!contents.isEmpty() && !"model".equals(rolAnterior)) {
            throw new IllegalArgumentException(
                    "El historial debe terminar con una respuesta de la IA");
        }

        // Agregar el mensaje actual
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(
                        Map.of("text", mensaje)
                )
        ));

        Map<String, Object> body = Map.of(
                "systemInstruction",
                Map.of(
                        "parts",
                        List.of(Map.of(
                                "text", SYSTEM_INSTRUCTION
                        ))
                ),
                "contents", contents,
                "generationConfig",
                Map.of(
                        "maxOutputTokens", 512,
                        "thinkingConfig", Map.of(
                                "thinkingLevel", "MINIMAL"
                        )
                )
        );

        // Aquí reutilizamos la conexión que ya funciona
        return enviarPeticionGemini(body);
    }



}