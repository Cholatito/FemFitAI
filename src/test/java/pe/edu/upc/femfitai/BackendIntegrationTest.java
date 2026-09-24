package pe.edu.upc.femfitai;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.femfitai.dtos.*;
import pe.edu.upc.femfitai.entities.*;
import pe.edu.upc.femfitai.repositories.*;
import pe.edu.upc.femfitai.services.interfaces.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.config.location=classpath:/application-test.properties")
class BackendIntegrationTest {
    @LocalServerPort int port;
    @Autowired UsuariosRepository usuarios;
    @Autowired CiclosRepository ciclos;
    @Autowired DetalleDiarioCicloRepository diarios;
    @Autowired RutinasRepository rutinas;
    @Autowired EjerciciosRepository ejercicios;
    @Autowired SesionesEntrenamientoRepository sesiones;
    @Autowired PerfilEntrenamientoRepository perfiles;
    @Autowired RecomendacionesIARepository recomendaciones;
    @Autowired IDetalleDiarioCicloService diarioService;
    @Autowired IPerfilEntrenamientoService perfilService;
    @Autowired IProgresoService progresoService;
    @Autowired IRutinaEjerciciosService rutinaEjerciciosService;
    @Autowired PasswordEncoder encoder;
    @Autowired DataSource dataSource;

    private final HttpClient client = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();
    private static final String PASSWORD = "OnlyForTests123!";

    @AfterEach void clearAuthentication() { SecurityContextHolder.clearContext(); }

    private Usuarios usuario(String rol) {
        return usuarios.saveAndFlush(new Usuarios("Test", "Backend", UUID.randomUUID() + "@example.test",
                encoder.encode(PASSWORD), rol, true, LocalDateTime.now()));
    }

    private void autenticar(Usuarios u) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(u.getCorreo(), null, List.of()));
    }

    private HttpResponse<String> request(String method, String path, String body, String token) throws Exception {
        var b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(15)).header("Content-Type", "application/json");
        if (token != null) b.header("Authorization", "Bearer " + token);
        return client.send(b.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private String token(Usuarios u) throws Exception {
        var response = request("POST", "/login", "{\"correo\":\"" + u.getCorreo()
                + "\",\"password\":\"" + PASSWORD + "\"}", null);
        assertEquals(200, response.statusCode(), response.body());
        return json.readTree(response.body()).get("accessToken").asText();
    }

    private Rutinas rutina(Usuarios u) {
        return rutinas.saveAndFlush(new Rutinas(u.getIdUsuario(), "Rutina", "Objetivo", "Inicial",
                LocalDateTime.now(), true));
    }

    private Ciclos ciclo(Usuarios u) {
        return ciclos.saveAndFlush(new Ciclos(u.getIdUsuario().longValue(), LocalDate.now().minusDays(2), null));
    }

    @Test void databaseIsExclusivelyInMemory() throws Exception {
        try (var connection = dataSource.getConnection()) {
            assertTrue(connection.getMetaData().getURL().startsWith("jdbc:h2:mem:"));
        }
    }

    @Test void registrationCanLoginAndPasswordsAreHashed() throws Exception {
        String correo = UUID.randomUUID() + "@example.test";
        String body = "{\"nombres\":\"Ana\",\"apellidos\":\"Test\",\"correo\":\"" + correo
                + "\",\"password\":\"" + PASSWORD + "\",\"rol\":\"ADMIN\"}";
        var registered = request("POST", "/usuarios", body, null);
        assertEquals(201, registered.statusCode(), registered.body());
        assertFalse(registered.body().contains(PASSWORD));
        Usuarios u = usuarios.findByCorreo(correo).orElseThrow();
        assertEquals("USUARIA", u.getRol());
        assertNotEquals(PASSWORD, u.getPasswordHash());
        assertTrue(encoder.matches(PASSWORD, u.getPasswordHash()));
        String jwt = token(u);
        assertEquals(200, request("GET", "/progreso", null, jwt).statusCode());
        assertEquals(409, request("POST", "/usuarios", body, null).statusCode());
        assertEquals(401, request("POST", "/login", "{\"correo\":\"" + correo
                + "\",\"password\":\"incorrecta\"}", null).statusCode());
    }

    @Test void catalogChangesRequireExistingAdministrativeRoles() throws Exception {
        var e = ejercicios.saveAndFlush(new Ejercicios("Original", "Piernas", "Fuerza", "Descripcion"));
        String path = "/ejercicios/" + e.getIdEjercicio();
        String body = "{\"nombre\":\"Actualizado\"}";
        assertEquals(401, request("PUT", path, body, null).statusCode());
        for (String rol : List.of("USUARIA", "TESTER")) {
            String jwt = token(usuario(rol));
            assertEquals(403, request("PUT", path, body, jwt).statusCode());
            assertEquals(403, request("DELETE", path, null, jwt).statusCode());
        }
        for (String rol : List.of("ADMIN", "PROGRAMADOR")) {
            assertEquals(200, request("PUT", path, body, token(usuario(rol))).statusCode());
        }
        assertEquals(204, request("DELETE", path, null, token(usuario("ADMIN"))).statusCode());
    }

    @Test void searchFiltersEscapesAndPaginates() throws Exception {
        String marker = UUID.randomUUID().toString();
        ejercicios.saveAndFlush(new Ejercicios(marker + " A", null, "Fuerza", null));
        ejercicios.saveAndFlush(new Ejercicios(marker + " B", null, "fuerza", null));
        ejercicios.saveAndFlush(new Ejercicios(marker + " C", null, "Cardio", null));
        ejercicios.saveAndFlush(new Ejercicios(marker + " %_!", null, "Fuerza", null));
        String jwt = token(usuario("USUARIA"));
        var response = request("GET", "/ejercicios/buscar?tipo=FUERZA&query=" + marker + "&tamano=1&pagina=1", null, jwt);
        assertEquals(200, response.statusCode(), response.body());
        JsonNode page = json.readTree(response.body());
        assertEquals(3, page.get("totalElements").asInt());
        // El registro con '%' ordena antes de A; la segunda pagina contiene A.
        assertEquals(marker + " A", page.get("content").get(0).get("nombre").asText());
        var literal = request("GET", "/ejercicios/buscar?query=" + marker + "%20%25_%21", null, jwt);
        assertEquals(1, json.readTree(literal.body()).get("totalElements").asInt());
        assertEquals(0, json.readTree(request("GET", "/ejercicios/buscar?query=missing-" + marker, null, jwt).body()).get("totalElements").asInt());
        assertEquals(400, request("GET", "/ejercicios/buscar?pagina=-1", null, jwt).statusCode());
        assertEquals(200, request("GET", "/ejercicios/buscar", null, jwt).statusCode());
    }

    @Test void dailyPhasesEnergyAndUpsertAreValidated() throws Exception {
        var u = usuario("USUARIA");
        var c = ciclo(u);
        String jwt = token(u);
        for (String phase : List.of("Menstrual", "Folicular", "Ovulatoria", "Lútea")) {
            long started = System.nanoTime();
            var response = request("POST", "/detalle-diario", "{\"idCiclo\":" + c.getIdCiclo()
                    + ",\"nivelEnergia\":3,\"faseRegistrada\":\"" + phase + "\"}", jwt);
            assertEquals(201, response.statusCode(), response.body());
            System.out.printf("H2 diagnostic only: daily HTTP save=%.3fms%n", (System.nanoTime() - started) / 1e6);
        }
        assertEquals(1, diarios.buscarPorUsuarioYFecha(u.getIdUsuario().longValue(), LocalDate.now()).size());
        for (String value : List.of("0", "6", "null")) {
            assertEquals(400, request("PUT", "/detalle-diario", "{\"idCiclo\":" + c.getIdCiclo()
                    + ",\"nivelEnergia\":" + value + "}", jwt).statusCode());
        }
        var invalid = request("POST", "/detalle-diario", "{\"idCiclo\":" + c.getIdCiclo()
                + ",\"nivelEnergia\":3,\"faseRegistrada\":\"Otra\"}", jwt);
        assertEquals(400, invalid.statusCode());
        assertEquals(400, json.readTree(invalid.body()).get("status").asInt());
        assertEquals(403, request("POST", "/detalle-diario", "{\"idCiclo\":" + c.getIdCiclo()
                + ",\"nivelEnergia\":3}", token(usuario("USUARIA"))).statusCode());
    }

    @Test void concurrentDailyRequestsForDifferentCyclesDoNotDuplicateDate() throws Exception {
        var u = usuario("USUARIA");
        var c1 = ciclo(u);
        var c2 = ciclo(u);
        runConcurrently(u, () -> diarioService.guardar(new DetalleDiarioCicloDTO(null,
                c1.getIdCiclo().intValue(), LocalDate.now(), "Menstrual", 3, "uno")),
                () -> diarioService.guardar(new DetalleDiarioCicloDTO(null,
                        c2.getIdCiclo().intValue(), LocalDate.now(), "Folicular", 4, "dos")));
        assertEquals(1, diarios.buscarPorUsuarioYFecha(u.getIdUsuario().longValue(), LocalDate.now()).size());
    }

    private void runConcurrently(Usuarios u, Runnable first, Runnable second) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (Runnable action : List.of(first, second)) {
                futures.add(executor.submit(() -> {
                    autenticar(u);
                    ready.countDown();
                    try {
                        if (!start.await(5, TimeUnit.SECONDS)) throw new AssertionError("Inicio concurrente agotado");
                        action.run();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError(ex);
                    } finally { SecurityContextHolder.clearContext(); }
                }));
            }
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            for (var future : futures) future.get(15, TimeUnit.SECONDS);
        } finally { executor.shutdownNow(); }
    }

    @Test void profilesRemainUniqueUnderConcurrentCreation() throws Exception {
        var u = usuario("USUARIA");
        var successes = new java.util.concurrent.atomic.AtomicInteger();
        var conflicts = new java.util.concurrent.atomic.AtomicInteger();
        Runnable create = () -> {
            try {
                perfilService.registrar(new PerfilEntrenamientoDTO(null, null, "Inicial", "Entrenar", 3, 30, LocalDate.of(2000, 1, 1)));
                successes.incrementAndGet();
            } catch (ResponseStatusException ex) {
                assertEquals(409, ex.getStatusCode().value());
                conflicts.incrementAndGet();
            }
        };
        runConcurrently(u, create, create);
        assertEquals(1, successes.get());
        assertEquals(1, conflicts.get());
        assertTrue(perfiles.findByIdUsuario(u.getIdUsuario()).isPresent());
        assertThrows(DataIntegrityViolationException.class, () -> perfiles.saveAndFlush(
                new PerfilEntrenamiento(u.getIdUsuario(), "Inicial", "Entrenar", 3, 30, LocalDate.of(2000, 1, 1))));
    }

    @Test void sessionPostAndPutEnforceOwnershipAndSameValidation() throws Exception {
        var owner = usuario("USUARIA");
        var other = usuario("USUARIA");
        var ownRoutine = rutina(owner);
        var otherRoutine = rutina(other);
        String jwt = token(owner);
        String body = "{\"idRutina\":" + ownRoutine.getIdRutina() + ",\"duracionMin\":30,\"nivelEnergia\":3,\"esfuerzoPercibido\":5,\"estado\":\"Completada\"}";
        var created = request("POST", "/sesiones", body, jwt);
        assertEquals(201, created.statusCode(), created.body());
        int id = json.readTree(created.body()).get("idSesion").asInt();
        var original = sesiones.findById(id).orElseThrow();
        assertEquals(owner.getIdUsuario(), original.getIdUsuario());
        assertEquals(LocalDate.now(), original.getFecha().toLocalDate());
        String path = "/sesiones/" + id;
        for (String invalid : List.of(body.replace("\"duracionMin\":30", "\"duracionMin\":-1"),
                body.replace("\"duracionMin\":30", "\"duracionMin\":null"),
                body.replace("\"esfuerzoPercibido\":5", "\"esfuerzoPercibido\":0"),
                body.replace("\"esfuerzoPercibido\":5", "\"esfuerzoPercibido\":null"),
                body.replace("\"esfuerzoPercibido\":5", "\"esfuerzoPercibido\":11"))) {
            assertEquals(400, request("POST", "/sesiones", invalid, jwt).statusCode());
            assertEquals(400, request("PUT", path, invalid, jwt).statusCode());
        }
        String stolen = body.replace("\"idRutina\":" + ownRoutine.getIdRutina(), "\"idRutina\":" + otherRoutine.getIdRutina());
        assertEquals(403, request("POST", "/sesiones", stolen, jwt).statusCode());
        assertEquals(403, request("PUT", path, stolen, jwt).statusCode());
        String transfer = body.replace("}", ",\"idUsuario\":" + other.getIdUsuario() + "}");
        assertEquals(403, request("PUT", path, transfer, jwt).statusCode());
        assertEquals(200, request("PUT", path, body.replace("}", ",\"fecha\":\"1999-01-01T00:00:00\"}"), jwt).statusCode());
        assertEquals(original.getFecha(), sesiones.findById(id).orElseThrow().getFecha());
        String otherJwt = token(other);
        assertEquals(403, request("GET", path, null, otherJwt).statusCode());
        assertEquals(403, request("GET", "/sesiones/detalle/" + id, null, otherJwt).statusCode());
        assertEquals(403, request("GET", "/sesiones/usuario/" + owner.getIdUsuario(), null, otherJwt).statusCode());
        assertEquals(403, request("PUT", path, body, otherJwt).statusCode());
        assertEquals(403, request("DELETE", path, null, otherJwt).statusCode());
        assertEquals(0, json.readTree(request("GET", "/sesiones", null, otherJwt).body()).size());
    }

    @Test void routineOwnershipCannotBeTakenThroughPut() throws Exception {
        var owner = usuario("USUARIA");
        var intruder = usuario("USUARIA");
        var r = rutina(owner);
        String responseBody = "{\"idUsuario\":" + intruder.getIdUsuario() + ",\"nombre\":\"Robada\"}";
        assertEquals(403, request("PUT", "/rutinas/" + r.getIdRutina(), responseBody, token(intruder)).statusCode());
        assertEquals(owner.getIdUsuario(), rutinas.findById(r.getIdRutina()).orElseThrow().getIdUsuario());
    }

    @Test void recommendationsArePrivateEmptyAndAcceptanceIsIdempotent() throws Exception {
        var u = usuario("USUARIA");
        String jwt = token(u);
        assertEquals(0, json.readTree(request("GET", "/recomendaciones", null, jwt).body()).get("totalElements").asInt());
        // Fixture persisted directly: this is not a pretend recommendation generator.
        var e = recomendaciones.saveAndFlush(new RecomendacionesIA(u.getIdUsuario(), null, LocalDateTime.now(),
                "Fixture", "Contenido de prueba", "Motivo de prueba", false));
        LocalDateTime storedDate = recomendaciones.findById(e.getIdRecomendacion()).orElseThrow().getFecha();
        String path = "/recomendaciones/" + e.getIdRecomendacion();
        var other = usuario("USUARIA");
        String otherJwt = token(other);
        assertEquals(403, request("GET", path, null, otherJwt).statusCode());
        assertEquals(403, request("PATCH", path + "/aceptar", null, otherJwt).statusCode());
        assertEquals(0, json.readTree(request("GET", "/recomendaciones", null, otherJwt).body()).get("totalElements").asInt());
        for (int i = 0; i < 2; i++) {
            var accepted = request("PATCH", path + "/aceptar", null, jwt);
            assertEquals(200, accepted.statusCode(), accepted.body());
            assertTrue(json.readTree(accepted.body()).get("aceptada").asBoolean());
        }
        var persisted = recomendaciones.findById(e.getIdRecomendacion()).orElseThrow();
        assertEquals(e.getContenido(), persisted.getContenido());
        assertEquals(e.getMotivo(), persisted.getMotivo());
        assertEquals(storedDate, persisted.getFecha());
        assertEquals(404, request("GET", "/recomendaciones/2147483647", null, jwt).statusCode());
        assertEquals(400, request("GET", "/recomendaciones?tamano=101", null, jwt).statusCode());
    }

    @Test void administrativeDeletionStillRejectsReferencedExercises() throws Exception {
        var u = usuario("USUARIA");
        autenticar(u);
        var r = rutina(u);
        var e = ejercicios.saveAndFlush(new Ejercicios("Referenciado", null, null, null));
        rutinaEjerciciosService.registrar(new RutinaEjerciciosDTO(null, r.getIdRutina(), e.getIdEjercicio(), 3, 10, 30));
        assertEquals(409, request("DELETE", "/ejercicios/" + e.getIdEjercicio(), null, token(usuario("ADMIN"))).statusCode());
        assertTrue(ejercicios.existsById(e.getIdEjercicio()));
    }

    @Test void historyJoinAndProfileQueriesKeepTheirBehaviorAndReportTimings() {
        var u = usuario("USUARIA");
        autenticar(u);
        perfilService.registrar(new PerfilEntrenamientoDTO(null, null, "Inicial", "Entrenar", 3, 30, LocalDate.of(2000, 1, 1)));
        for (int i = 0; i < 25; i++) progresoService.registrar(new ProgresoDTO(null, null,
                LocalDate.now().minusDays(i), new BigDecimal("60.00"), null, "Nota"));
        var r = rutina(u);
        var e = ejercicios.saveAndFlush(new Ejercicios("Ejercicio fixture", "Piernas", "Fuerza", "Descripcion"));
        rutinaEjerciciosService.registrar(new RutinaEjerciciosDTO(null, r.getIdRutina(), e.getIdEjercicio(), 3, 10, 60));
        long start = System.nanoTime();
        var page = progresoService.listar(1, 10);
        long history = System.nanoTime() - start;
        assertEquals(25, page.getTotalElements());
        assertEquals(10, page.getNumberOfElements());
        assertEquals(LocalDate.now().minusDays(10), page.getContent().get(0).fecha());
        start = System.nanoTime();
        var detail = rutinaEjerciciosService.listarPorRutina(r.getIdRutina());
        long join = System.nanoTime() - start;
        assertEquals(1, detail.size());
        assertEquals(60, detail.get(0).descansoSeg());
        assertEquals("Ejercicio fixture", detail.get(0).nombre());
        start = System.nanoTime();
        assertEquals(u.getIdUsuario(), perfilService.consultar().idUsuario());
        long profile = System.nanoTime() - start;
        System.out.printf("H2 diagnostic only: history=%.3fms join=%.3fms profile=%.3fms%n",
                history / 1e6, join / 1e6, profile / 1e6);
        // No thresholds: this fixture is not a production PostgreSQL performance certification.
    }
}
