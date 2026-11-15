package pe.edu.upc.femfitai.services.implementations;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;


@Service
public class GeminiService {

    private final RestClient restClient;
    private final String model;

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
        """;

    public GeminiService(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model) {

        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
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
                        "maxOutputTokens", 300,
                        "temperature", 0.7
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
}