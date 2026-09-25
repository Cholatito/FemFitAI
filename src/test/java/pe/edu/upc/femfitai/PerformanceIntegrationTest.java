package pe.edu.upc.femfitai;

import java.math.BigDecimal;
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
import pe.edu.upc.femfitai.entities.Ciclos;
import pe.edu.upc.femfitai.entities.Ejercicios;
import pe.edu.upc.femfitai.entities.PerfilEntrenamiento;
import pe.edu.upc.femfitai.entities.Progreso;
import pe.edu.upc.femfitai.entities.RutinaEjercicios;
import pe.edu.upc.femfitai.entities.Rutinas;
import pe.edu.upc.femfitai.entities.Usuarios;
import pe.edu.upc.femfitai.repositories.CiclosRepository;
import pe.edu.upc.femfitai.repositories.EjerciciosRepository;
import pe.edu.upc.femfitai.repositories.PerfilEntrenamientoRepository;
import pe.edu.upc.femfitai.repositories.ProgresoRepository;
import pe.edu.upc.femfitai.repositories.RutinaEjerciciosRepository;
import pe.edu.upc.femfitai.repositories.RutinasRepository;
import pe.edu.upc.femfitai.repositories.UsuariosRepository;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.config.location=classpath:/application-test.properties")
class PerformanceIntegrationTest {
    @LocalServerPort int port;
    @Autowired UsuariosRepository usuarios;
    @Autowired CiclosRepository ciclos;
    @Autowired EjerciciosRepository ejercicios;
    @Autowired PerfilEntrenamientoRepository perfiles;
    @Autowired ProgresoRepository progresos;
    @Autowired RutinaEjerciciosRepository rutinaEjercicios;
    @Autowired RutinasRepository rutinas;
    @Autowired PasswordEncoder encoder;

    private final HttpClient client = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();
    private Usuarios owner;
    private String jwt;

    @BeforeEach void prepare() throws Exception {
        String password = "OnlyForTests123!";
        owner = usuarios.saveAndFlush(new Usuarios("Perf", "Test", UUID.randomUUID() + "@example.test",
                encoder.encode(password), "USUARIA", true, LocalDateTime.now()));
        var login = request("POST", "/login", "{\"correo\":\"" + owner.getCorreo()
                + "\",\"password\":\"" + password + "\"}", null);
        assertEquals(200, login.statusCode(), login.body());
        jwt = json.readTree(login.body()).get("accessToken").textValue();
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

    @Test void us11DailyDetailEndpointIsMeasuredWithOneRecord() throws Exception {
        var cycle = ciclos.saveAndFlush(new Ciclos(owner.getIdUsuario().longValue(), LocalDate.now().minusDays(1), null));
        long started = System.nanoTime();
        var response = request("POST", "/detalle-diario", "{\"idCiclo\":" + cycle.getIdCiclo()
                + ",\"nivelEnergia\":3,\"faseRegistrada\":\"Menstrual\"}", jwt);
        long elapsed = System.nanoTime() - started;
        assertEquals(201, response.statusCode(), response.body());
        long elapsedMillis = Duration.ofNanos(elapsed).toMillis();
        System.out.println("H2 diagnostic only: US11 endpoint=POST /detalle-diario records=1 elapsed_ms=" + elapsedMillis);
    }

    @Test void us15HistoryEndpointIsMeasuredWithOneHundredRecords() throws Exception {
        for (int index = 0; index < 100; index++) {
            progresos.save(new Progreso(owner.getIdUsuario(), LocalDate.now().minusDays(index),
                    new BigDecimal("60.00"), null, "Nota"));
        }
        progresos.flush();
        long started = System.nanoTime();
        var response = request("GET", "/progreso?pagina=0&tamano=100", null, jwt);
        long elapsedMillis = Duration.ofNanos(System.nanoTime() - started).toMillis();
        assertEquals(200, response.statusCode(), response.body());
        System.out.println("US15 endpoint=GET /progreso records=100 elapsed_ms=" + elapsedMillis + " limit_ms=not_defined_in_repo");
    }

    @Test void us25RoutineJoinEndpointIsMeasuredWithTwentyFiveRelations() throws Exception {
        var routine = rutinas.saveAndFlush(new Rutinas(owner.getIdUsuario(), "Rendimiento", "Objetivo", "Inicial",
                LocalDateTime.now(), true));
        for (int index = 0; index < 25; index++) {
            var exercise = ejercicios.saveAndFlush(new Ejercicios("Ejercicio perf " + index, "Piernas", "Fuerza", null));
            rutinaEjercicios.save(new RutinaEjercicios(routine.getIdRutina(), exercise.getIdEjercicio(), 3, 10, 60));
        }
        rutinaEjercicios.flush();
        long started = System.nanoTime();
        var response = request("GET", "/rutina-ejercicios/rutina/" + routine.getIdRutina(), null, jwt);
        long elapsedMillis = Duration.ofNanos(System.nanoTime() - started).toMillis();
        assertEquals(200, response.statusCode(), response.body());
        System.out.println("US25 endpoint=GET /rutina-ejercicios/rutina/{id} records=25 elapsed_ms="
                + elapsedMillis + " limit_ms=not_defined_in_repo");
    }

    @Test void us30ExerciseSearchEndpointIsMeasuredWithOneHundredExercises() throws Exception {
        for (int index = 0; index < 100; index++) {
            ejercicios.save(new Ejercicios("Catalogo perf " + index, "Piernas", "Fuerza", "Descripcion"));
        }
        ejercicios.flush();
        long started = System.nanoTime();
        var response = request("GET", "/ejercicios/buscar?tipo=Fuerza&tamano=100&pagina=0", null, jwt);
        long elapsedMillis = Duration.ofNanos(System.nanoTime() - started).toMillis();
        assertEquals(200, response.statusCode(), response.body());
        System.out.println("US30 endpoint=GET /ejercicios/buscar records=100 elapsed_ms=" + elapsedMillis
                + " limit_ms=not_defined_in_repo");
    }

    @Test void us41ProfileEndpointIsMeasuredWithOneProfile() throws Exception {
        perfiles.saveAndFlush(new PerfilEntrenamiento(owner.getIdUsuario(), "Inicial", "Entrenar", 3, 30,
                LocalDate.of(2000, 1, 1)));
        long started = System.nanoTime();
        var response = request("GET", "/perfiles", null, jwt);
        long elapsedMillis = Duration.ofNanos(System.nanoTime() - started).toMillis();
        assertEquals(200, response.statusCode(), response.body());
        System.out.println("US41 endpoint=GET /perfiles records=1 elapsed_ms=" + elapsedMillis
                + " limit_ms=not_defined_in_repo");
    }
}
