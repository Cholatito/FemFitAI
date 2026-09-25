package pe.edu.upc.femfitai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upc.femfitai.entities.PerfilEntrenamiento;
import pe.edu.upc.femfitai.entities.Usuarios;
import pe.edu.upc.femfitai.repositories.PerfilEntrenamientoRepository;
import pe.edu.upc.femfitai.repositories.RecomendacionesIARepository;
import pe.edu.upc.femfitai.repositories.UsuariosRepository;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.config.location=classpath:/application-test.properties")
class GeneracionRecomendacionesRealIntegrationTest {
    @LocalServerPort int port;
    @Autowired UsuariosRepository usuarios;
    @Autowired PerfilEntrenamientoRepository perfiles;
    @Autowired RecomendacionesIARepository recomendaciones;
    @Autowired PasswordEncoder encoder;

    private final HttpClient client = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();
    private Usuarios owner;
    private String jwt;

    @BeforeEach void prepare() throws Exception {
        String password = "OnlyForTests123!";
        owner = usuarios.saveAndFlush(new Usuarios("Test", "GeneracionReal", UUID.randomUUID() + "@example.test",
                encoder.encode(password), "USUARIA", true, LocalDateTime.now()));
        perfiles.saveAndFlush(new PerfilEntrenamiento(owner.getIdUsuario(), "Inicial", "Entrenar", 3, 30,
                LocalDate.of(2000, 1, 1)));
        var login = request("POST", "/login", "{\"correo\":\"" + owner.getCorreo()
                + "\",\"password\":\"" + password + "\"}", null);
        assertEquals(200, login.statusCode(), login.body());
        jwt = json.readTree(login.body()).get("accessToken").asText();
    }

    private HttpResponse<String> request(String method, String path, String body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(15)).header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return client.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test void internalGeneratorCreatesAndPersistsProfileBasedRecommendation() throws Exception {
        long before = recomendaciones.count();
        long started = System.nanoTime();
        var response = request("POST", "/recomendaciones/generar", "{}", jwt);
        long elapsedNanos = System.nanoTime() - started;

        assertEquals(201, response.statusCode(), response.body());
        assertEquals(before + 1, recomendaciones.count());
        assertTrue(json.readTree(response.body()).get("contenido").asText().contains("Plan recomendado"));
        System.out.println("H2 diagnostic only: profile based generation elapsed ms="
                + Duration.ofNanos(elapsedNanos).toMillis());
    }
}
