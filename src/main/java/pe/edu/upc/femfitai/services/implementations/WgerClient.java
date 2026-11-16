
package pe.edu.upc.femfitai.services.implementations;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class WgerClient {

    private static final int LIMITE = 20;

    private final RestClient restClient;

    public WgerClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://wger.de/api/v2")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public Map<String, Object> obtenerPaginaEjercicios(int offset) {

        if (offset < 0) {
            throw new IllegalArgumentException("El offset no puede ser negativo");
        }

        try {
            Map<String, Object> respuesta = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/exerciseinfo/")
                            .queryParam("limit", LIMITE)
                            .queryParam("offset", offset)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (respuesta == null || !respuesta.containsKey("results")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "La respuesta de wger no tiene el formato esperado"
                );
            }

            return respuesta;

        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo consultar la API externa de wger",
                    ex
            );
        }
    }
}
