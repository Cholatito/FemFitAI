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
    @Autowired DetalleSesionRepository detallesSesion;
    @Autowired DetalleSerieRepository series;
    @Autowired IDetalleDiarioCicloService diarioService;
    @Autowired IPerfilEntrenamientoService perfilService;
    @Autowired IProgresoService progresoService;
    @Autowired IRutinaEjerciciosService rutinaEjerciciosService;
    @Autowired PasswordEncoder encoder;
    @Autowired DataSource dataSource;
    @Autowired org.springframework.security.oauth2.jwt.JwtEncoder jwtEncoder;

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

    @Test void openApiPublishesFemFitAiTitleAndBearerJwtScheme() throws Exception {
        var response = request("GET", "/v3/api-docs", null, null);
        assertEquals(200, response.statusCode(), response.body());
        JsonNode document = json.readTree(response.body());
        assertEquals("FemFitAI API", document.get("info").get("title").textValue());
        assertEquals("bearer", document.get("components").get("securitySchemes")
            .get("bearerAuth").get("scheme").textValue());
        assertEquals("JWT", document.get("components").get("securitySchemes")
            .get("bearerAuth").get("bearerFormat").textValue());
    }

    @Test void userDetailsArePrivateAndAdministrativePermissionsRemainAvailable() throws Exception {
        var owner = usuario("USUARIA");
        String path = "/usuarios/" + owner.getIdUsuario();
        assertEquals(401, request("GET", path, null, null).statusCode());
        assertEquals(200, request("GET", path, null, token(owner)).statusCode());
        for (String role : List.of("USUARIA", "TESTER")) {
            String jwt = token(usuario(role));
            var denied = request("GET", path, null, jwt);
            assertEquals(403, denied.statusCode(), denied.body());
            assertEquals(403, json.readTree(denied.body()).get("status").asInt());
            assertEquals(path, json.readTree(denied.body()).get("path").asText());
            assertFalse(denied.body().contains(owner.getCorreo()));
            assertEquals(403, request("GET", "/usuarios", null, jwt).statusCode());
            assertEquals(403, request("PUT", path, "{}", jwt).statusCode());
            assertEquals(403, request("DELETE", path, null, jwt).statusCode());
        }
        for (String role : List.of("ADMIN", "PROGRAMADOR")) {
            String jwt = token(usuario(role));
            assertEquals(200, request("GET", path, null, jwt).statusCode());
            assertEquals(200, request("GET", "/usuarios", null, jwt).statusCode());
            assertEquals(400, request("PUT", path, "{}", jwt).statusCode());
            var disposable = usuario("USUARIA");
            assertEquals(204, request("DELETE", "/usuarios/" + disposable.getIdUsuario(), null, jwt).statusCode());
            assertFalse(usuarios.existsById(disposable.getIdUsuario()));
        }
    }

    @Test void expiredAndTamperedJwtCannotAccessPrivateEndpoints() throws Exception {
        var u = usuario("USUARIA");
        String valid = token(u);
        int signature = valid.lastIndexOf('.') + 1;
        String tampered = valid.substring(0, signature) + (valid.charAt(signature) == 'A' ? 'B' : 'A')
                + valid.substring(signature + 1);
        var claims = org.springframework.security.oauth2.jwt.JwtClaimsSet.builder()
                .subject(u.getCorreo()).issuedAt(Instant.now().minusSeconds(7200))
                .expiresAt(Instant.now().minusSeconds(3600)).claim("roles", "ROLE_USUARIA").build();
        var header = org.springframework.security.oauth2.jwt.JwsHeader
                .with(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS512).build();
        String expired = jwtEncoder.encode(org.springframework.security.oauth2.jwt.JwtEncoderParameters
                .from(header, claims)).getTokenValue();
        for (String jwt : List.of(tampered, expired, "invalid")) {
            assertEquals(401, request("GET", "/progreso", null, jwt).statusCode());
        }
        assertEquals(200, request("GET", "/progreso", null, valid).statusCode());
    }

    @Test void blankRegistrationPasswordsAreRejectedWithoutInventingComplexityRules() throws Exception {
        for (String password : List.of("null", "\"\"", "\"   \"")) {
            String correo = UUID.randomUUID() + "@example.test";
            var response = request("POST", "/usuarios", "{\"nombres\":\"Ana\",\"apellidos\":\"Test\",\"correo\":\""
                    + correo + "\",\"password\":" + password + "}", null);
            assertEquals(400, response.statusCode(), response.body());
            assertEquals(400, json.readTree(response.body()).get("status").asInt());
            assertTrue(usuarios.findByCorreo(correo).isEmpty());
        }
    }

    @Test void generationUsesTheProductionGeneratorAndHandlesOwnershipValidation() throws Exception {
        var u = usuario("USUARIA");
        String jwt = token(u);
        perfiles.saveAndFlush(new PerfilEntrenamiento(u.getIdUsuario(), "Inicial", "Entrenar", 3, 30, LocalDate.of(2000, 1, 1)));
        long before = recomendaciones.count();
        assertEquals(401, request("POST", "/recomendaciones/generar", "{}", null).statusCode());
        var created = request("POST", "/recomendaciones/generar", "{}", jwt);
        assertEquals(201, created.statusCode(), created.body());
        assertTrue(json.readTree(created.body()).get("contenido").asText().contains("perfil")
                || json.readTree(created.body()).get("contenido").asText().contains("Plan recomendado"));
        assertEquals(before + 1, recomendaciones.count());
        var foreignRoutine = rutina(usuario("USUARIA"));
        assertEquals(403, request("POST", "/recomendaciones/generar",
                "{\"idRutina\":" + foreignRoutine.getIdRutina() + "}", jwt).statusCode());
        assertEquals(404, request("POST", "/recomendaciones/generar", "{\"idRutina\":2147483647}", jwt).statusCode());
        assertEquals(400, request("POST", "/recomendaciones/generar", "{\"idRutina\":0}", jwt).statusCode());
        assertEquals(400, request("POST", "/recomendaciones/generar", "{\"idRutina\":2147483648}", jwt).statusCode());

        perfiles.deleteAll();
        assertEquals(400, request("POST", "/recomendaciones/generar", "{}", jwt).statusCode());
    }

    @Test void currentRpeBoundariesPersistOnPostAndPut() throws Exception {
        var u = usuario("USUARIA");
        var r = rutina(u);
        String jwt = token(u);
        for (int rpe : List.of(1, 10)) {
            String body = "{\"idRutina\":" + r.getIdRutina() + ",\"duracionMin\":1,\"nivelEnergia\":3,\"esfuerzoPercibido\":" + rpe + "}";
            var created = request("POST", "/sesiones", body, jwt);
            assertEquals(201, created.statusCode(), created.body());
            int id = json.readTree(created.body()).get("idSesion").asInt();
            assertEquals(rpe, sesiones.findById(id).orElseThrow().getEsfuerzoPercibido());
            assertEquals(200, request("PUT", "/sesiones/" + id, body, jwt).statusCode());
            var fetched = request("GET", "/sesiones/" + id, null, jwt);
            assertEquals(200, fetched.statusCode());
            assertEquals(rpe, json.readTree(fetched.body()).get("esfuerzoPercibido").asInt());
        }
    }

    @Test void cyclesAndRoutinesCannotBeReadModifiedOrTransferredByAnotherAccount() throws Exception {
        var owner = usuario("USUARIA");
        var other = usuario("USUARIA");
        String ownerJwt = token(owner);
        String otherJwt = token(other);
        var c = ciclo(owner);
        var r = rutina(owner);
        String cyclePath = "/ciclos/" + c.getIdCiclo();
        String routinePath = "/rutinas/" + r.getIdRutina();
        String cycleBody = "{\"idUsuario\":" + other.getIdUsuario() + ",\"fechaInicio\":\"" + LocalDate.now() + "\"}";
        String routineBody = "{\"idUsuario\":" + other.getIdUsuario() + ",\"nombre\":\"Transferencia\"}";
        for (String path : List.of(cyclePath, "/ciclos/detalle/" + c.getIdCiclo(),
                "/ciclos/usuario/" + owner.getIdUsuario(), routinePath, "/rutinas/usuario/" + owner.getIdUsuario())) {
            assertEquals(403, request("GET", path, null, otherJwt).statusCode(), path);
        }
        for (String jwt : List.of(ownerJwt, otherJwt)) {
            assertEquals(403, request("PUT", cyclePath, cycleBody, jwt).statusCode());
            assertEquals(403, request("PUT", routinePath, routineBody, jwt).statusCode());
        }
        assertEquals(403, request("DELETE", cyclePath, null, otherJwt).statusCode());
        assertEquals(403, request("DELETE", routinePath, null, otherJwt).statusCode());
        assertEquals(0, json.readTree(request("GET", "/ciclos", null, otherJwt).body()).size());
        assertEquals(0, json.readTree(request("GET", "/rutinas", null, otherJwt).body()).size());
        assertEquals(owner.getIdUsuario().longValue(), ciclos.findById(c.getIdCiclo()).orElseThrow().getIdUsuario());
        assertEquals(owner.getIdUsuario(), rutinas.findById(r.getIdRutina()).orElseThrow().getIdUsuario());
        // Long se conserva en ciclos por compatibilidad, sin conversion que trunque IDs.
        assertEquals(404, request("DELETE", "/ciclos/4294967297", null, ownerJwt).statusCode());
        assertTrue(ciclos.existsById(c.getIdCiclo()));
    }

    @Test void sessionDetailsAndSeriesEnforceOwnerAndKeepReferencesOnPut() throws Exception {
        var owner = usuario("USUARIA");
        var other = usuario("USUARIA");
        var r = rutina(owner);
        var session = sesiones.saveAndFlush(new SesionesEntrenamiento(r.getIdRutina(), owner.getIdUsuario(),
                LocalDateTime.now(), 30, 3, 5, "Completada"));
        var e = ejercicios.saveAndFlush(new Ejercicios("Privacidad", null, null, null));
        String jwt = token(owner);
        String otherJwt = token(other);
        String detailBody = "{\"idSesion\":" + session.getIdSesion() + ",\"idEjercicio\":" + e.getIdEjercicio() + "}";
        var detail = request("POST", "/detalle-sesion", detailBody, jwt);
        assertEquals(201, detail.statusCode(), detail.body());
        int detailId = json.readTree(detail.body()).get("idDetalle").asInt();
        assertEquals(404, request("POST", "/detalle-sesion",
            "{\"idSesion\":2147483647,\"idEjercicio\":" + e.getIdEjercicio() + "}", jwt).statusCode());
        assertEquals(404, request("POST", "/detalle-sesion",
            "{\"idSesion\":" + session.getIdSesion() + ",\"idEjercicio\":2147483647}", jwt).statusCode());
        String seriesBody = "{\"idDetalle\":" + detailId + ",\"numeroSerie\":1,\"repeticiones\":10,\"pesoKg\":20}";
        var createdSeries = request("POST", "/detalle-serie", seriesBody, jwt);
        assertEquals(201, createdSeries.statusCode(), createdSeries.body());
        int seriesId = json.readTree(createdSeries.body()).get("idSerie").asInt();
        for (String invalid : List.of("{\"repeticiones\":0,\"pesoKg\":20}",
            "{\"repeticiones\":-1,\"pesoKg\":20}",
            "{\"repeticiones\":10,\"pesoKg\":-1}")) {
            assertEquals(400, request("PUT", "/detalle-serie/" + seriesId, invalid, jwt).statusCode());
        }
        assertEquals(403, request("POST", "/detalle-sesion", detailBody, otherJwt).statusCode());
        assertEquals(403, request("POST", "/detalle-serie", seriesBody, otherJwt).statusCode());
        assertEquals(403, request("GET", "/detalle-sesion/sesion/" + session.getIdSesion(), null, otherJwt).statusCode());
        assertEquals(403, request("GET", "/detalle-serie/detalle/" + detailId, null, otherJwt).statusCode());
        assertEquals(403, request("PUT", "/detalle-sesion/" + detailId, "{\"observacion\":\"Ajena\"}", otherJwt).statusCode());
        assertEquals(400, request("PUT", "/detalle-sesion/" + detailId,
            "{\"observacion\":\"" + "O".repeat(256) + "\"}", jwt).statusCode());
        assertEquals(403, request("PUT", "/detalle-serie/" + seriesId, "{\"repeticiones\":12,\"pesoKg\":25}", otherJwt).statusCode());
        assertEquals(403, request("DELETE", "/detalle-sesion/" + detailId, null, otherJwt).statusCode());
        assertEquals(403, request("DELETE", "/detalle-serie/" + seriesId, null, otherJwt).statusCode());
        assertEquals(404, request("DELETE", "/detalle-serie/2147483647", null, jwt).statusCode());
        assertEquals(200, request("PUT", "/detalle-sesion/" + detailId,
                "{\"observacion\":\"Propia\",\"idSesion\":2147483647}", jwt).statusCode());
        assertEquals(200, request("PUT", "/detalle-serie/" + seriesId,
                "{\"repeticiones\":12,\"pesoKg\":25,\"idDetalle\":2147483647}", jwt).statusCode());
        assertEquals(session.getIdSesion(), detallesSesion.findById(detailId).orElseThrow().getIdSesion());
        assertEquals("Propia", detallesSesion.findById(detailId).orElseThrow().getObservacion());
        assertEquals(detailId, series.findById(seriesId).orElseThrow().getIdDetalle());
        assertEquals(12, series.findById(seriesId).orElseThrow().getRepeticiones());
        var zeroWeight = request("POST", "/detalle-serie", "{\"idDetalle\":" + detailId
                + ",\"numeroSerie\":2,\"repeticiones\":10,\"pesoKg\":0}", jwt);
        assertEquals(201, zeroWeight.statusCode(), zeroWeight.body());
        int zeroWeightId = json.readTree(zeroWeight.body()).get("idSerie").asInt();
        assertEquals(0, series.findById(zeroWeightId).orElseThrow().getPesoKg().signum());
        assertEquals(409, request("DELETE", "/detalle-sesion/" + detailId, null, jwt).statusCode());
        assertEquals(204, request("DELETE", "/detalle-serie/" + seriesId, null, jwt).statusCode());
        assertEquals(204, request("DELETE", "/detalle-serie/" + zeroWeightId, null, jwt).statusCode());
        assertEquals(204, request("DELETE", "/detalle-sesion/" + detailId, null, jwt).statusCode());
        assertFalse(series.existsById(seriesId));
        assertFalse(detallesSesion.existsById(detailId));
    }

    @Test void historyRoutineAndProfileEndpointsPersistAndRemainPrivateWithDiagnosticTimings() throws Exception {
        var u = usuario("USUARIA");
        var other = usuario("USUARIA");
        String jwt = token(u);
        String otherJwt = token(other);
        assertEquals(404, request("GET", "/perfiles", null, jwt).statusCode());
        assertEquals(201, request("POST", "/perfiles", "{\"nivelEntrenamiento\":\"Inicial\",\"objetivoPrincipal\":\"Entrenar\",\"diasDisponibles\":3,\"tiempoDisponible\":30,\"fechaNacimiento\":\"2000-01-01\"}", jwt).statusCode());
        var progress = request("POST", "/progreso", "{\"fecha\":\"" + LocalDate.now() + "\",\"pesoKg\":60,\"notaPersonal\":\"Prueba\"}", jwt);
        assertEquals(201, progress.statusCode(), progress.body());
        int progressId = json.readTree(progress.body()).get("idProgreso").asInt();
        var r = rutina(u);
        var e = ejercicios.saveAndFlush(new Ejercicios("Diagnostico", "Piernas", "Fuerza", "Prueba"));
        var relation = request("POST", "/rutina-ejercicios", "{\"idRutina\":" + r.getIdRutina()
                + ",\"idEjercicio\":" + e.getIdEjercicio() + ",\"series\":3,\"repeticiones\":10,\"descansoSeg\":60}", jwt);
        assertEquals(201, relation.statusCode(), relation.body());
        for (String path : List.of("/progreso", "/rutina-ejercicios/rutina/" + r.getIdRutina(), "/perfiles")) {
            long start = System.nanoTime();
            var response = request("GET", path, null, jwt);
            System.out.printf("H2 diagnostic only: GET %s = %.3fms%n", path, (System.nanoTime() - start) / 1e6);
            assertEquals(200, response.statusCode(), response.body());
        }
        assertEquals(403, request("GET", "/progreso/" + progressId, null, otherJwt).statusCode());
        assertEquals(0, json.readTree(request("GET", "/progreso", null, otherJwt).body()).get("totalElements").asInt());
        assertEquals(403, request("GET", "/rutina-ejercicios/rutina/" + r.getIdRutina(), null, otherJwt).statusCode());
        assertEquals(404, request("GET", "/perfiles", null, otherJwt).statusCode());
        assertEquals(404, request("PUT", "/perfiles", "{\"nivelEntrenamiento\":\"Inicial\",\"objetivoPrincipal\":\"Otro\"}", otherJwt).statusCode());
        assertEquals("Entrenar", perfiles.findByIdUsuario(u.getIdUsuario()).orElseThrow().getObjetivoPrincipal());
    }

    @Test void detailSeriesValidateRepetitionsWeightAndExistingDetail() throws Exception {
        var owner = usuario("USUARIA");
        var routine = rutina(owner);
        var session = sesiones.saveAndFlush(new SesionesEntrenamiento(routine.getIdRutina(), owner.getIdUsuario(),
                LocalDateTime.now(), 30, 3, 5, "Completada"));
        var exercise = ejercicios.saveAndFlush(new Ejercicios("Serie validacion", null, null, null));
        var detail = detallesSesion.saveAndFlush(new DetalleSesion(session.getIdSesion(), exercise.getIdEjercicio(), null));
        String jwt = token(owner);
        long before = series.count();
        for (String body : List.of(
                "{\"idDetalle\":" + detail.getIdDetalle() + ",\"numeroSerie\":1,\"repeticiones\":0,\"pesoKg\":1}",
                "{\"idDetalle\":" + detail.getIdDetalle() + ",\"numeroSerie\":1,\"repeticiones\":1,\"pesoKg\":-1}",
                "{\"idDetalle\":" + detail.getIdDetalle() + ",\"numeroSerie\":1,\"repeticiones\":null,\"pesoKg\":1}",
                "{\"idDetalle\":2147483647,\"numeroSerie\":1,\"repeticiones\":1,\"pesoKg\":1}")) {
            var response = request("POST", "/detalle-serie", body, jwt);
            assertTrue(response.statusCode() == 400 || response.statusCode() == 404, response.body());
            assertEquals(before, series.count());
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

        @Test void exerciseLimitsAndDescriptionPayloadsAreValidated() throws Exception {
        String jwt = token(usuario("ADMIN"));
        String validName = "N".repeat(100);
        String validType = "T".repeat(50);
        String normalDescription = "Descripcion normal: movilidad < 3 y control de respiracion";
        var created = request("POST", "/ejercicios", "{\"nombre\":\"" + validName
            + "\",\"tipo\":\"" + validType + "\",\"descripcion\":\""
            + normalDescription + "\"}", jwt);
        assertEquals(201, created.statusCode(), created.body());
        int id = json.readTree(created.body()).get("idEjercicio").asInt();
        assertEquals(org.springframework.web.util.HtmlUtils.htmlEscape(normalDescription),
                ejercicios.findById(id).orElseThrow().getDescripcion());
        assertEquals(400, request("POST", "/ejercicios", "{\"nombre\":\""
            + "N".repeat(101) + "\"}", jwt).statusCode());
        assertEquals(400, request("POST", "/ejercicios", "{\"nombre\":\"Nombre\",\"tipo\":\""
            + "T".repeat(51) + "\"}", jwt).statusCode());
        for (String payload : List.of("<script>alert(1)</script>", "<img src=x onerror=alert(1)>",
            "javascript:alert(1)")) {
            var invalid = request("POST", "/ejercicios", "{\"nombre\":\"Nombre\",\"descripcion\":\""
                + payload + "\"}", jwt);
            assertEquals(400, invalid.statusCode(), invalid.body());
            assertEquals(400, json.readTree(invalid.body()).get("status").asInt());
        }
        var invalidUpdate = request("PUT", "/ejercicios/" + id,
            "{\"nombre\":\"Nombre\",\"descripcion\":\"<script>x</script>\"}", jwt);
        assertEquals(400, invalidUpdate.statusCode(), invalidUpdate.body());
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
            var persisted = diarios.buscarPorUsuarioYFecha(u.getIdUsuario().longValue(), LocalDate.now()).get(0);
            assertEquals(phase, persisted.getFaseRegistrada());
            assertEquals(3, persisted.getNivelEnergia());
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

        @Test void dailyObservationsAreOptionalAndRespectDatabaseLimit() throws Exception {
        var u = usuario("USUARIA");
        var c = ciclo(u);
        String jwt = token(u);
        var optional = request("POST", "/detalle-diario", "{\"idCiclo\":" + c.getIdCiclo()
            + ",\"nivelEnergia\":3,\"observaciones\":null}", jwt);
        assertEquals(201, optional.statusCode(), optional.body());
        String observations = "O".repeat(255);
        var maximum = request("PUT", "/detalle-diario", "{\"idCiclo\":" + c.getIdCiclo()
            + ",\"nivelEnergia\":3,\"observaciones\":\"" + observations + "\"}", jwt);
        assertEquals(200, maximum.statusCode(), maximum.body());
        assertEquals(observations, diarios.buscarPorUsuarioYFecha(u.getIdUsuario().longValue(), LocalDate.now())
            .get(0).getObservaciones());
        var tooLong = request("PUT", "/detalle-diario", "{\"idCiclo\":" + c.getIdCiclo()
            + ",\"nivelEnergia\":3,\"observaciones\":\"" + "O".repeat(256) + "\"}", jwt);
        assertEquals(400, tooLong.statusCode(), tooLong.body());
        assertEquals(400, json.readTree(tooLong.body()).get("status").asInt());
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
