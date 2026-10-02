package pe.edu.upc.femfitai;

import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.femfitai.entities.*;
import pe.edu.upc.femfitai.repositories.*;
import pe.edu.upc.femfitai.services.interfaces.GeneradorRecomendaciones;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// El doble existe exclusivamente en test: verifica la integracion, no una IA real.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.config.location=classpath:/application-test.properties")
class GeneracionRecomendacionesIntegrationTest {
    @LocalServerPort int port;
    @Autowired UsuariosRepository usuarios;
    @Autowired RutinasRepository rutinas;
    @Autowired PerfilEntrenamientoRepository perfiles;
    @Autowired RecomendacionesIARepository recomendaciones;
    @Autowired PasswordEncoder encoder;
    @MockitoBean GeneradorRecomendaciones generador;
    private final HttpClient client = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();
    private Usuarios owner;
    private Rutinas routine;
    private String jwt;

    @BeforeEach void prepare() throws Exception {
        String password = UUID.randomUUID().toString();
        owner = usuarios.saveAndFlush(new Usuarios("Test", "Generacion", UUID.randomUUID() + "@example.test",
                encoder.encode(password), "USUARIA", true, LocalDateTime.now()));
        routine = rutinas.saveAndFlush(new Rutinas(owner.getIdUsuario(), "Prueba", "Prueba", "Inicial", LocalDateTime.now(), true));
        var login = request("POST", "/login", "{\"correo\":\"" + owner.getCorreo() + "\",\"password\":\"" + password + "\"}");
        assertEquals(200, login.statusCode());
        jwt = json.readTree(login.body()).get("accessToken").asText();
    }

    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(15)).header("Content-Type", "application/json");
        if (jwt != null) builder.header("Authorization", "Bearer " + jwt);
        return client.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test void adapterResultIsPersistedForTokenOwnerAndWorksWithExistingReadAndAccept() throws Exception {
        when(generador.generar(owner.getIdUsuario(), routine.getIdRutina()))
                .thenReturn(new GeneradorRecomendaciones.Resultado("Fixture", "Contenido de prueba", "Motivo de prueba"));
        var response = request("POST", "/recomendaciones/generar", "{\"idRutina\":" + routine.getIdRutina()
                + ",\"idUsuario\":2147483647,\"aceptada\":true,\"fecha\":\"1999-01-01T00:00:00\"}");
        assertEquals(201, response.statusCode(), response.body());
        int id = json.readTree(response.body()).get("idRecomendacion").asInt();
        var saved = recomendaciones.findById(id).orElseThrow();
        assertEquals(owner.getIdUsuario(), saved.getIdUsuario());
        assertEquals(routine.getIdRutina(), saved.getIdRutina());
        assertEquals("Contenido de prueba", saved.getContenido());
        assertEquals("Motivo de prueba", saved.getMotivo());
        assertEquals(LocalDate.now(), saved.getFecha().toLocalDate());
        assertFalse(saved.getAceptada());
        String path = "/recomendaciones/" + id;
        assertEquals(path, response.headers().firstValue("Location").orElseThrow());
        assertEquals(200, request("GET", path, null).statusCode());
        assertEquals(200, request("PATCH", path + "/aceptar", null).statusCode());
        assertTrue(recomendaciones.findById(id).orElseThrow().getAceptada());
        verify(generador).generar(owner.getIdUsuario(), routine.getIdRutina());
    }

    @Test void optionalRoutineDoesNotInventARequiredBusinessInput() throws Exception {
        when(generador.generar(owner.getIdUsuario(), null))
                .thenReturn(new GeneradorRecomendaciones.Resultado(null, "Fixture", "Fixture"));
        var response = request("POST", "/recomendaciones/generar", "{}");
        assertEquals(201, response.statusCode(), response.body());
        var saved = recomendaciones.findById(json.readTree(response.body()).get("idRecomendacion").asInt()).orElseThrow();
        assertNull(saved.getIdRutina());
        assertEquals(owner.getIdUsuario(), saved.getIdUsuario());
    }

    @Test void foreignRoutineNeverReachesGenerator() throws Exception {
        var foreign = usuarios.saveAndFlush(new Usuarios("Otra", "Cuenta", UUID.randomUUID() + "@example.test",
                owner.getPasswordHash(), "USUARIA", true, LocalDateTime.now()));
        var otherRoutine = rutinas.saveAndFlush(new Rutinas(foreign.getIdUsuario(), "Otra", "Otra", "Inicial", LocalDateTime.now(), true));
        long before = recomendaciones.count();
        var response = request("POST", "/recomendaciones/generar", "{\"idRutina\":" + otherRoutine.getIdRutina() + "}");
        assertEquals(403, response.statusCode());
        verifyNoInteractions(generador);
        assertEquals(before, recomendaciones.count());
    }

    @Test void insufficientDataReportedByFutureAdapterIsHandledWithoutSaving() throws Exception {
        // La regla no se define aqui: el doble comprueba la propagacion del error.
        when(generador.generar(owner.getIdUsuario(), null)).thenThrow(new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Complete los datos requeridos por la regla del generador"));
        long before = recomendaciones.count();
        var response = request("POST", "/recomendaciones/generar", "{}");
        assertEquals(400, response.statusCode());
        assertEquals(400, json.readTree(response.body()).get("status").asInt());
        assertTrue(json.readTree(response.body()).get("message").asText().contains("Complete"));
        assertEquals(before, recomendaciones.count());
    }

    @Test void malformedAdapterOutputIsNotPersisted() throws Exception {
        long before = recomendaciones.count();
        for (var result : java.util.Arrays.asList(null,
                new GeneradorRecomendaciones.Resultado("Fixture", null, "Motivo"),
                new GeneradorRecomendaciones.Resultado("Fixture", "", "Motivo"),
                new GeneradorRecomendaciones.Resultado("Fixture", " ", "Motivo"),
                new GeneradorRecomendaciones.Resultado("Fixture", "Contenido", null),
                new GeneradorRecomendaciones.Resultado("Fixture", "Contenido", ""),
                new GeneradorRecomendaciones.Resultado("Fixture", "Contenido", "   "),
                new GeneradorRecomendaciones.Resultado("x".repeat(51), "Contenido", "Motivo"))) {
            when(generador.generar(owner.getIdUsuario(), null)).thenReturn(result);
            var response = request("POST", "/recomendaciones/generar", "{}");
            assertEquals(502, response.statusCode(), response.body());
            assertEquals(502, json.readTree(response.body()).get("status").asInt());
            assertEquals(before, recomendaciones.count());
        }
    }

    @Test void unexpectedGenerationErrorUsesGlobalHandlerAndLeavesReadEndpointsAvailable() throws Exception {
        long before = recomendaciones.count();
        when(generador.generar(owner.getIdUsuario(), null)).thenThrow(new IllegalStateException("internal-test-detail"));
        var response = request("POST", "/recomendaciones/generar", "{}");
        assertEquals(500, response.statusCode());
        assertEquals(500, json.readTree(response.body()).get("status").asInt());
        assertFalse(response.body().contains("internal-test-detail"));
        assertEquals(before, recomendaciones.count());
        assertEquals(200, request("GET", "/recomendaciones", null).statusCode());
    }
}
