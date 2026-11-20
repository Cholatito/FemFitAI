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
    @Autowired ProgresoRepository progresos;
    @Autowired RutinaEjerciciosRepository rutinaEjerciciosRepository;
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
        return rutinas.saveAndFlush(new Rutinas(u, "Rutina", "Objetivo", "Inicial",
                LocalDateTime.now(), true));
    }

    private Ciclos ciclo(Usuarios u) {
        return ciclos.saveAndFlush(new Ciclos(u, LocalDate.now().minusDays(2), null));
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
        var owner = usuario("TESTER");
        String path = "/usuarios/" + owner.getIdUsuario();
        assertEquals(401, request("GET", path, null, null).statusCode());
        assertEquals(200, request("GET", path, null, token(owner)).statusCode());
        for (String role : List.of("TESTER")) {
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
        for (String role : List.of("PROGRAMADOR")) {
            String jwt = token(usuario(role));
            assertEquals(200, request("GET", path, null, jwt).statusCode());
            assertEquals(200, request("GET", "/usuarios", null, jwt).statusCode());
            assertEquals(400, request("PUT", path, "{}", jwt).statusCode());
            var disposable = usuario("TESTER");
            assertEquals(204, request("DELETE", "/usuarios/" + disposable.getIdUsuario(), null, jwt).statusCode());
            assertFalse(usuarios.findById(disposable.getIdUsuario()).orElseThrow().getEstado());
        }
    }

    @Test void expiredAndTamperedJwtCannotAccessPrivateEndpoints() throws Exception {
        var u = usuario("TESTER");
        String valid = token(u);
        int signature = valid.lastIndexOf('.') + 1;
        String tampered = valid.substring(0, signature) + (valid.charAt(signature) == 'A' ? 'B' : 'A')
                + valid.substring(signature + 1);
        var claims = org.springframework.security.oauth2.jwt.JwtClaimsSet.builder()
                .subject(u.getCorreo()).issuedAt(Instant.now().minusSeconds(7200))
                .expiresAt(Instant.now().minusSeconds(3600)).claim("roles", "ROLE_TESTER").build();
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

    @Test void registrationRejectsPasswordsMissingEachRequiredCriterion() throws Exception {
        for (String password : List.of("Ab1cde", "abcdefgh1", "ABCDEFGH1", "Abcdefgh")) {
            String correo = UUID.randomUUID() + "@example.test";
            String body = "{\"nombres\":\"Ana\",\"apellidos\":\"Test\",\"correo\":\""
                    + correo + "\",\"password\":\"" + password + "\"}";
            var response = request("POST", "/usuarios", body, null);
            assertEquals(400, response.statusCode(), response.body());
            assertTrue(usuarios.findByCorreo(correo).isEmpty());
        }
    }

    @Test void cycleEstimatedEndIsCalculatedAndRecalculatedAtTwentyEightCalendarDays() throws Exception {
        var owner = usuario("TESTER");
        String jwt = token(owner);
        LocalDate start = LocalDate.of(2025, 1, 31);
        String postBody = "{\"idUsuario\":" + owner.getIdUsuario() + ",\"fechaInicio\":\"" + start
                + "\",\"fechaFinEstimada\":\"2025-02-01\"}";
        var created = request("POST", "/ciclos", postBody, jwt);
        assertEquals(200, created.statusCode(), created.body());
        long cycleId = json.readTree(created.body()).get("idCiclo").asLong();
        LocalDate expected = start.plusDays(28);
        assertEquals(expected.toString(), json.readTree(created.body()).get("fechaFinEstimada").asText());
        assertEquals(expected, ciclos.findById(cycleId).orElseThrow().getFechaFinEstimada());

        LocalDate updatedStart = LocalDate.of(2025, 2, 28);
        String putBody = "{\"idUsuario\":" + owner.getIdUsuario() + ",\"fechaInicio\":\"" + updatedStart
                + "\",\"fechaFinEstimada\":\"2025-03-01\"}";
        var updated = request("PUT", "/ciclos/" + cycleId, putBody, jwt);
        assertEquals(200, updated.statusCode(), updated.body());
        LocalDate updatedExpected = updatedStart.plusDays(28);
        assertEquals(updatedExpected.toString(), json.readTree(updated.body()).get("fechaFinEstimada").asText());
        assertEquals(updatedExpected, ciclos.findById(cycleId).orElseThrow().getFechaFinEstimada());
    }

    @Test void generationUsesTheProductionGeneratorAndHandlesOwnershipValidation() throws Exception {
        var u = usuario("TESTER");
        String jwt = token(u);
        var routine = rutina(u);
        perfiles.saveAndFlush(new PerfilEntrenamiento(u, "Inicial", "Entrenar", 3, 30, LocalDate.of(2000, 1, 1)));
        long before = recomendaciones.count();
        assertEquals(401, request("POST", "/recomendaciones/generar", "{}", null).statusCode());
        var created = request("POST", "/recomendaciones/generar",
                "{\"idRutina\":" + routine.getIdRutina() + "}", jwt);
        assertEquals(201, created.statusCode(), created.body());
        assertTrue(json.readTree(created.body()).get("contenido").asText().contains("perfil")
                || json.readTree(created.body()).get("contenido").asText().contains("Plan recomendado"));
        assertEquals(before + 1, recomendaciones.count());
        var foreignRoutine = rutina(usuario("TESTER"));
        assertEquals(403, request("POST", "/recomendaciones/generar",
                "{\"idRutina\":" + foreignRoutine.getIdRutina() + "}", jwt).statusCode());
        assertEquals(404, request("POST", "/recomendaciones/generar", "{\"idRutina\":2147483647}", jwt).statusCode());
        assertEquals(400, request("POST", "/recomendaciones/generar", "{\"idRutina\":0}", jwt).statusCode());
        assertEquals(400, request("POST", "/recomendaciones/generar", "{\"idRutina\":2147483648}", jwt).statusCode());

        perfiles.deleteAll();
        assertEquals(400, request("POST", "/recomendaciones/generar",
                "{\"idRutina\":" + routine.getIdRutina() + "}", jwt).statusCode());
    }

    @Test void currentRpeBoundariesPersistOnPostAndPut() throws Exception {
        var u = usuario("TESTER");
        var r = rutina(u);
        String jwt = token(u);
        String body = "{\"idRutina\":" + r.getIdRutina() + ",\"duracionMin\":1,\"nivelEnergia\":3,\"esfuerzoPercibido\":10}";
        assertEquals(405, request("POST", "/sesiones", body, jwt).statusCode());
        assertEquals(0, sesiones.count());
        var created = request("POST", "/sesiones/rutina/" + r.getIdRutina() + "/iniciar", null, jwt);
        assertEquals(201, created.statusCode(), created.body());
        int id = json.readTree(created.body()).get("idSesion").asInt();
        assertNull(sesiones.findById(id).orElseThrow().getDuracionMin());
        assertNull(sesiones.findById(id).orElseThrow().getNivelEnergia());
        assertNull(sesiones.findById(id).orElseThrow().getEsfuerzoPercibido());
        assertEquals(405, request("PUT", "/sesiones/" + id, body, jwt).statusCode());
        var fetched = request("GET", "/sesiones/" + id, null, jwt);
        assertEquals(200, fetched.statusCode());
        assertEquals("EN_CURSO", json.readTree(fetched.body()).get("estado").asText());
    }

    @Test void cycleUpdateRejectsInvalidDatesAndMissingIdsWithoutChangingStoredCycle() throws Exception {
        var owner = usuario("TESTER");
        String jwt = token(owner);
        var cycle = ciclos.saveAndFlush(new Ciclos(owner, LocalDate.now().minusDays(2), null));
        long initialCount = ciclos.count();
        LocalDate initialStart = cycle.getFechaInicio();
        String path = "/ciclos/" + cycle.getIdCiclo();

        for (String fechaInicio : List.of("null", "\"fecha-invalida\"")) {
            String body = "{\"idUsuario\":" + owner.getIdUsuario() + ",\"fechaInicio\":" + fechaInicio + "}";
            var response = request("PUT", path, body, jwt);
            assertEquals(400, response.statusCode(), response.body());
            JsonNode error = json.readTree(response.body());
            assertEquals(400, error.get("status").asInt());
            assertEquals(path, error.get("path").asText());
            if ("null".equals(fechaInicio)) {
                assertTrue(error.get("message").asText().contains("fechaInicio"));
            } else {
                assertTrue(error.get("message").asText().contains("Peticion invalida"));
            }
            assertEquals(initialCount, ciclos.count());
            var unchanged = ciclos.findById(cycle.getIdCiclo()).orElseThrow();
            assertEquals(initialStart, unchanged.getFechaInicio());
            assertNull(unchanged.getFechaFinEstimada());
        }

        String validBody = "{\"idUsuario\":" + owner.getIdUsuario()
                + ",\"fechaInicio\":\"" + LocalDate.now().minusDays(1) + "\"}";
        var missing = request("PUT", "/ciclos/2147483647", validBody, jwt);
        assertEquals(404, missing.statusCode(), missing.body());
        JsonNode error = json.readTree(missing.body());
        assertEquals(404, error.get("status").asInt());
        assertEquals("/ciclos/2147483647", error.get("path").asText());
        assertTrue(error.get("message").asText().contains("No existe un ciclo"));
        assertEquals(initialCount, ciclos.count());
        var unchanged = ciclos.findById(cycle.getIdCiclo()).orElseThrow();
        assertEquals(initialStart, unchanged.getFechaInicio());
        assertNull(unchanged.getFechaFinEstimada());
    }

    @Test void cyclesAndRoutinesCannotBeReadModifiedOrTransferredByAnotherAccount() throws Exception {
        var owner = usuario("TESTER");
        var other = usuario("TESTER");
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
        assertEquals(owner.getIdUsuario().longValue(), ciclos.findById(c.getIdCiclo()).orElseThrow().getUsuario().getIdUsuario().longValue());
        assertEquals(owner.getIdUsuario(), rutinas.findById(r.getIdRutina()).orElseThrow().getUsuario().getIdUsuario());
        // Long se conserva en ciclos por compatibilidad, sin conversion que trunque IDs.
        assertEquals(404, request("DELETE", "/ciclos/4294967297", null, ownerJwt).statusCode());
        assertTrue(ciclos.existsById(c.getIdCiclo()));
    }

    @Test void sessionDetailsAndSeriesEnforceOwnerAndKeepReferencesOnPut() throws Exception {
        var owner = usuario("TESTER");
        var other = usuario("TESTER");
        var r = rutina(owner);
        var planned = ejercicios.saveAndFlush(new Ejercicios("Planificado", null, null, null));
        var extra = ejercicios.saveAndFlush(new Ejercicios("Adicional", null, null, null));
        String jwt = token(owner);
        String otherJwt = token(other);
        var relation = request("POST", "/rutina-ejercicios",
                "{\"idRutina\":" + r.getIdRutina() + ",\"idEjercicio\":" + planned.getIdEjercicio()
                        + ",\"series\":3,\"repeticiones\":10,\"descansoSeg\":60}", jwt);
        assertEquals(201, relation.statusCode(), relation.body());
        var started = request("POST", "/sesiones/rutina/" + r.getIdRutina() + "/iniciar", null, jwt);
        assertEquals(201, started.statusCode(), started.body());
        int sessionId = json.readTree(started.body()).get("idSesion").asInt();
        DetalleSesion copied = detallesSesion.findAll().stream()
                .filter(d -> d.getSesion().getIdSesion().equals(sessionId))
                .filter(d -> d.getEjercicio().getIdEjercicio().equals(planned.getIdEjercicio()))
                .findFirst().orElseThrow();
        int detailId = copied.getIdDetalle();
        assertEquals(1, detallesSesion.findAll().stream()
                .filter(d -> d.getSesion().getIdSesion().equals(sessionId))
                .filter(d -> d.getEjercicio().getIdEjercicio().equals(planned.getIdEjercicio()))
                .count());
        long detailCount = detallesSesion.count();
        String detailBody = "{\"idSesion\":" + sessionId + ",\"idEjercicio\":" + extra.getIdEjercicio() + "}";
        var detail = request("POST", "/detalle-sesion", detailBody, jwt);
        assertEquals(201, detail.statusCode(), detail.body());
        int extraDetailId = json.readTree(detail.body()).get("idDetalle").asInt();
        assertEquals(detailCount + 1, detallesSesion.count());
        assertEquals(404, request("POST", "/detalle-sesion",
                "{\"idSesion\":2147483647,\"idEjercicio\":" + extra.getIdEjercicio() + "}", jwt).statusCode());
        assertEquals(404, request("POST", "/detalle-sesion",
                "{\"idSesion\":" + sessionId + ",\"idEjercicio\":2147483647}", jwt).statusCode());
        assertEquals(extra.getIdEjercicio(), detallesSesion.findById(extraDetailId).orElseThrow().getEjercicio().getIdEjercicio());
        String seriesBody = "{\"idDetalle\":" + extraDetailId + ",\"numeroSerie\":1,\"repeticiones\":10,\"pesoKg\":20}";
        var createdSeries = request("POST", "/detalle-serie", seriesBody, jwt);
        assertEquals(201, createdSeries.statusCode(), createdSeries.body());
        int seriesId = json.readTree(createdSeries.body()).get("idSerie").asInt();
        assertEquals(new BigDecimal("20.00"), series.findById(seriesId).orElseThrow().getPesoKg());
        assertEquals(10, series.findById(seriesId).orElseThrow().getRepeticiones());
        for (String invalid : List.of("{\"repeticiones\":0,\"pesoKg\":20}",
                "{\"repeticiones\":-1,\"pesoKg\":20}",
                "{\"repeticiones\":10,\"pesoKg\":-1}")) {
            assertEquals(400, request("PUT", "/detalle-serie/" + seriesId, invalid, jwt).statusCode());
            assertEquals(10, series.findById(seriesId).orElseThrow().getRepeticiones());
            assertEquals(new BigDecimal("20.00"), series.findById(seriesId).orElseThrow().getPesoKg());
        }
        assertEquals(403, request("POST", "/detalle-sesion", detailBody, otherJwt).statusCode());
        assertEquals(403, request("POST", "/detalle-serie", seriesBody, otherJwt).statusCode());
        assertEquals(403, request("GET", "/detalle-sesion/sesion/" + sessionId, null, otherJwt).statusCode());
        assertEquals(403, request("GET", "/detalle-serie/detalle/" + extraDetailId, null, otherJwt).statusCode());
        assertEquals(403, request("PUT", "/detalle-sesion/" + extraDetailId, "{\"observacion\":\"Ajena\"}", otherJwt).statusCode());
        assertEquals(400, request("PUT", "/detalle-sesion/" + extraDetailId,
                "{\"observacion\":\"" + "O".repeat(256) + "\"}", jwt).statusCode());
        assertNull(detallesSesion.findById(extraDetailId).orElseThrow().getObservacion());
        assertEquals(403, request("PUT", "/detalle-serie/" + seriesId, "{\"repeticiones\":12,\"pesoKg\":25}", otherJwt).statusCode());
        assertEquals(403, request("DELETE", "/detalle-sesion/" + extraDetailId, null, otherJwt).statusCode());
        assertEquals(403, request("DELETE", "/detalle-serie/" + seriesId, null, otherJwt).statusCode());
        assertEquals(404, request("DELETE", "/detalle-serie/2147483647", null, jwt).statusCode());
        assertEquals(200, request("PUT", "/detalle-sesion/" + extraDetailId,
                "{\"observacion\":\"" + "O".repeat(255) + "\",\"idSesion\":2147483647,\"idEjercicio\":2147483647}", jwt).statusCode());
        assertEquals(200, request("PUT", "/detalle-serie/" + seriesId,
                "{\"repeticiones\":12,\"pesoKg\":25,\"idDetalle\":2147483647}", jwt).statusCode());
        assertEquals(sessionId, detallesSesion.findById(extraDetailId).orElseThrow().getSesion().getIdSesion());
        assertEquals("O".repeat(255), detallesSesion.findById(extraDetailId).orElseThrow().getObservacion());
        assertEquals(extra.getIdEjercicio(), detallesSesion.findById(extraDetailId).orElseThrow().getEjercicio().getIdEjercicio());
        assertEquals(extraDetailId, series.findById(seriesId).orElseThrow().getDetalle().getIdDetalle());
        assertEquals(12, series.findById(seriesId).orElseThrow().getRepeticiones());
        var zeroWeight = request("POST", "/detalle-serie", "{\"idDetalle\":" + extraDetailId
                + ",\"numeroSerie\":2,\"repeticiones\":10,\"pesoKg\":0}", jwt);
        assertEquals(201, zeroWeight.statusCode(), zeroWeight.body());
        int zeroWeightId = json.readTree(zeroWeight.body()).get("idSerie").asInt();
        assertEquals(0, series.findById(zeroWeightId).orElseThrow().getPesoKg().signum());
        assertEquals(409, request("DELETE", "/detalle-sesion/" + extraDetailId, null, jwt).statusCode());
        assertTrue(detallesSesion.existsById(detailId));
        assertTrue(series.existsById(seriesId));
        assertEquals(204, request("DELETE", "/detalle-serie/" + seriesId, null, jwt).statusCode());
        assertEquals(204, request("DELETE", "/detalle-serie/" + zeroWeightId, null, jwt).statusCode());
        assertEquals(204, request("DELETE", "/detalle-sesion/" + detailId, null, jwt).statusCode());
        assertFalse(series.existsById(seriesId));
        assertFalse(detallesSesion.existsById(detailId));
        assertEquals(404, request("DELETE", "/detalle-sesion/" + detailId, null, jwt).statusCode());
    }

    @Test void routineExerciseDeleteIsProtectedAndPersists() throws Exception {
        var owner = usuario("TESTER");
        var other = usuario("TESTER");
        String ownerJwt = token(owner);
        String otherJwt = token(other);

        var r = rutina(owner);
        var e = ejercicios.saveAndFlush(new Ejercicios("Press banca", "Pecho", "Fuerza", "Descripcion"));
        var relation = request("POST", "/rutina-ejercicios", "{\"idRutina\":" + r.getIdRutina()
                + ",\"idEjercicio\":" + e.getIdEjercicio() + ",\"series\":3,\"repeticiones\":10,\"descansoSeg\":60}", ownerJwt);
        assertEquals(201, relation.statusCode(), relation.body());
        int relationId = json.readTree(relation.body()).get("idRutinaEjercicio").asInt();

        assertEquals(204, request("DELETE", "/rutina-ejercicios/" + relationId, null, ownerJwt).statusCode());
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(owner.getCorreo(), null, List.of()));
        assertFalse(rutinaEjerciciosService.listarPorRutina(r.getIdRutina()).stream()
                .anyMatch(x -> x.idRutinaEjercicio().equals(relationId)));
        assertFalse(rutinaEjerciciosRepository.existsById(relationId));

        var r2 = rutina(owner);
        var e2 = ejercicios.saveAndFlush(new Ejercicios("Sentadilla", "Piernas", "Fuerza", "Descripcion"));
        var relation2 = request("POST", "/rutina-ejercicios", "{\"idRutina\":" + r2.getIdRutina()
                + ",\"idEjercicio\":" + e2.getIdEjercicio() + ",\"series\":4,\"repeticiones\":8,\"descansoSeg\":60}", ownerJwt);
        int relationId2 = json.readTree(relation2.body()).get("idRutinaEjercicio").asInt();

        assertEquals(403, request("DELETE", "/rutina-ejercicios/" + relationId2, null, otherJwt).statusCode());
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(owner.getCorreo(), null, List.of()));
        assertTrue(rutinaEjerciciosRepository.existsById(relationId2));
        assertEquals(404, request("DELETE", "/rutina-ejercicios/2147483647", null, ownerJwt).statusCode());

        var routineOwned = rutinas.findById(r2.getIdRutina()).orElseThrow();
        var exerciseOwned = ejercicios.findById(e2.getIdEjercicio()).orElseThrow();
        assertEquals(owner.getIdUsuario(), routineOwned.getUsuario().getIdUsuario());
        assertEquals(e2.getIdEjercicio(), exerciseOwned.getIdEjercicio());
    }

    @Test void routineSessionCanStartFromOwnRoutineAndAvoidDuplicates() throws Exception {
        var owner = usuario("TESTER");
        var other = usuario("TESTER");
        String ownerJwt = token(owner);
        String otherJwt = token(other);

        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(owner.getCorreo(), null, List.of()));
        var routine = rutina(owner);
        var exerciseA = ejercicios.saveAndFlush(new Ejercicios("Hip thrust", "Glúteos", "Fuerza", "Descripcion"));
        var exerciseB = ejercicios.saveAndFlush(new Ejercicios("Sentadilla", "Piernas", "Fuerza", "Descripcion"));
        rutinaEjerciciosService.registrar(new RutinaEjerciciosDTO(null, routine.getIdRutina(), exerciseA.getIdEjercicio(), 4, 12, 90));
        rutinaEjerciciosService.registrar(new RutinaEjerciciosDTO(null, routine.getIdRutina(), exerciseB.getIdEjercicio(), 3, 10, 60));

        long beforeDetalles = detallesSesion.count();
        long beforeSeries = series.count();

        var created = request("POST", "/sesiones/rutina/" + routine.getIdRutina() + "/iniciar", null, ownerJwt);
        assertEquals(201, created.statusCode(), created.body());
        int sessionId = json.readTree(created.body()).get("idSesion").asInt();
        var session = sesiones.findById(sessionId).orElseThrow();
        assertEquals(owner.getIdUsuario(), session.getUsuario().getIdUsuario());
        assertEquals(routine.getIdRutina(), session.getRutina().getIdRutina());
        assertEquals("EN_CURSO", session.getEstado());
        assertNull(session.getDuracionMin());
        assertEquals(beforeDetalles + 2, detallesSesion.count());
        assertEquals(beforeSeries, series.count());
        long copied = detallesSesion.findAll().stream()
                .filter(d -> d.getSesion().getIdSesion().equals(sessionId))
                .count();
        assertEquals(2, copied);

        var duplicate = request("POST", "/sesiones/rutina/" + routine.getIdRutina() + "/iniciar", null, ownerJwt);
        assertEquals(409, duplicate.statusCode(), duplicate.body());
        assertEquals(403, request("POST", "/sesiones/rutina/" + routine.getIdRutina() + "/iniciar", null, otherJwt).statusCode());
        assertEquals(404, request("POST", "/sesiones/rutina/2147483647/iniciar", null, ownerJwt).statusCode());
    }

    @Test void sessionFinalizationPersistsOwnerDecisionAndKeepsHistory() throws Exception {
        var owner = usuario("TESTER");
        var other = usuario("TESTER");
        var routine = rutina(owner);
        var session = sesiones.saveAndFlush(new SesionesEntrenamiento(routine, owner,
                LocalDateTime.now(), null, null, null, "EN_CURSO"));
        var exercise = ejercicios.saveAndFlush(new Ejercicios("Press banca", "Pecho", "Fuerza", "Descripcion"));
        var detail = detallesSesion.saveAndFlush(new DetalleSesion(session, exercise, "Listo"));
        var seriesRow = series.saveAndFlush(new DetalleSerie(detail, 1, 10, new BigDecimal("20.00")));

        String ownerJwt = token(owner);
        String otherJwt = token(other);
        String body = "{\"duracionMin\":45,\"nivelEnergia\":4,\"esfuerzoPercibido\":7}";

        var finished = request("PUT", "/sesiones/" + session.getIdSesion() + "/finalizar", body, ownerJwt);
        assertEquals(200, finished.statusCode(), finished.body());
        var persisted = sesiones.findById(session.getIdSesion()).orElseThrow();
        assertEquals(45, persisted.getDuracionMin());
        assertEquals(4, persisted.getNivelEnergia());
        assertEquals(7, persisted.getEsfuerzoPercibido());
        assertEquals("FINALIZADA", persisted.getEstado());
        assertTrue(detallesSesion.existsById(detail.getIdDetalle()));
        assertTrue(series.existsById(seriesRow.getIdSerie()));

        assertEquals(403, request("PUT", "/sesiones/" + session.getIdSesion() + "/finalizar", body, otherJwt).statusCode());
        assertEquals(404, request("PUT", "/sesiones/2147483647/finalizar", body, ownerJwt).statusCode());

        var alreadyFinished = sesiones.saveAndFlush(new SesionesEntrenamiento(routine, owner,
                LocalDateTime.now(), 30, 3, 5, "FINALIZADA"));
        assertEquals(409, request("PUT", "/sesiones/" + alreadyFinished.getIdSesion() + "/finalizar", body, ownerJwt).statusCode());

        var invalidSession = sesiones.saveAndFlush(new SesionesEntrenamiento(routine, owner,
                LocalDateTime.now(), null, null, null, "EN_CURSO"));
        for (String invalid : List.of(
                "{\"duracionMin\":0,\"nivelEnergia\":4,\"esfuerzoPercibido\":7}",
                "{\"duracionMin\":30,\"nivelEnergia\":0,\"esfuerzoPercibido\":7}",
                "{\"duracionMin\":30,\"nivelEnergia\":3,\"esfuerzoPercibido\":11}")) {
            assertEquals(400, request("PUT", "/sesiones/" + invalidSession.getIdSesion() + "/finalizar", invalid, ownerJwt).statusCode());
        }
    }

    @Test void sessionFlowFromRoutineToFinalizationKeepsPlannedExercisesIndependent() throws Exception {
        var owner = usuario("TESTER");
        var other = usuario("TESTER");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(owner.getCorreo(), null, List.of()));
        var routine = rutina(owner);
        var exercisePlanA = ejercicios.saveAndFlush(new Ejercicios("Hip thrust", "Glúteos", "Fuerza", "Descripcion"));
        var exercisePlanB = ejercicios.saveAndFlush(new Ejercicios("Sentadilla", "Piernas", "Fuerza", "Descripcion"));
        var exerciseExtra = ejercicios.saveAndFlush(new Ejercicios("Peso muerto", "Glúteos", "Fuerza", "Descripcion"));
        rutinaEjerciciosService.registrar(new RutinaEjerciciosDTO(null, routine.getIdRutina(), exercisePlanA.getIdEjercicio(), 4, 12, 90));
        rutinaEjerciciosService.registrar(new RutinaEjerciciosDTO(null, routine.getIdRutina(), exercisePlanB.getIdEjercicio(), 3, 10, 60));

        String ownerJwt = token(owner);
        String otherJwt = token(other);

        assertEquals(403, request("POST", "/sesiones/rutina/" + routine.getIdRutina() + "/iniciar", null, otherJwt).statusCode());
        var started = request("POST", "/sesiones/rutina/" + routine.getIdRutina() + "/iniciar", null, ownerJwt);
        assertEquals(201, started.statusCode(), started.body());
        int sessionId = json.readTree(started.body()).get("idSesion").asInt();
        var detalleList = request("GET", "/detalle-sesion/sesion/" + sessionId, null, ownerJwt);
        assertEquals(200, detalleList.statusCode(), detalleList.body());
        JsonNode detalles = json.readTree(detalleList.body());
        assertEquals(2, detalles.size());
        List<Integer> plannedIds = new ArrayList<>();
        for (JsonNode detalle : detalles) {
            plannedIds.add(detalle.get("idEjercicio").asInt());
        }
        assertTrue(plannedIds.contains(exercisePlanA.getIdEjercicio()));
        assertTrue(plannedIds.contains(exercisePlanB.getIdEjercicio()));

        var extraDetail = request("POST", "/detalle-sesion", "{\"idSesion\":" + sessionId + ",\"idEjercicio\":" + exerciseExtra.getIdEjercicio() + "}", ownerJwt);
        assertEquals(201, extraDetail.statusCode(), extraDetail.body());
        int extraDetailId = json.readTree(extraDetail.body()).get("idDetalle").asInt();

        var seriesCreate = request("POST", "/detalle-serie", "{\"idDetalle\":" + detalles.get(0).get("idDetalle").asInt() + ",\"numeroSerie\":1,\"repeticiones\":8,\"pesoKg\":20}", ownerJwt);
        assertEquals(201, seriesCreate.statusCode(), seriesCreate.body());
        int seriesId = json.readTree(seriesCreate.body()).get("idSerie").asInt();

        var updatedSeries = request("PUT", "/detalle-serie/" + seriesId, "{\"repeticiones\":10,\"pesoKg\":22.5}", ownerJwt);
        assertEquals(200, updatedSeries.statusCode(), updatedSeries.body());
        assertEquals(10, series.findById(seriesId).orElseThrow().getRepeticiones());
        assertEquals(new BigDecimal("22.50"), series.findById(seriesId).orElseThrow().getPesoKg());

        assertEquals(204, request("DELETE", "/detalle-serie/" + seriesId, null, ownerJwt).statusCode());
        assertFalse(series.existsById(seriesId));

        var routineAfterSession = rutinaEjerciciosService.listarPorRutina(routine.getIdRutina());
        assertEquals(2, routineAfterSession.size());
        assertEquals(2, rutinaEjerciciosRepository.findByRutina_IdRutinaOrderByIdRutinaEjercicioAsc(routine.getIdRutina()).size());

        var finish = request("PUT", "/sesiones/" + sessionId + "/finalizar", "{\"duracionMin\":50,\"nivelEnergia\":5,\"esfuerzoPercibido\":8}", ownerJwt);
        assertEquals(200, finish.statusCode(), finish.body());
        assertEquals("FINALIZADA", sesiones.findById(sessionId).orElseThrow().getEstado());
        assertEquals(50, sesiones.findById(sessionId).orElseThrow().getDuracionMin());
        assertEquals(5, sesiones.findById(sessionId).orElseThrow().getNivelEnergia());
        assertEquals(8, sesiones.findById(sessionId).orElseThrow().getEsfuerzoPercibido());

        var startedAgain = request("POST", "/sesiones/rutina/" + routine.getIdRutina() + "/iniciar", null, ownerJwt);
        assertEquals(201, startedAgain.statusCode(), startedAgain.body());
        assertEquals("EN_CURSO", sesiones.findById(json.readTree(startedAgain.body()).get("idSesion").asInt()).orElseThrow().getEstado());
        assertTrue(detallesSesion.existsById(extraDetailId));
    }

    @Test void historyRoutineAndProfileEndpointsPersistAndRemainPrivateWithDiagnosticTimings() throws Exception {
        var u = usuario("TESTER");
        var other = usuario("TESTER");
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
        assertEquals("Entrenar", perfiles.findByUsuario_IdUsuario(u.getIdUsuario()).orElseThrow().getObjetivoPrincipal());
    }

    @Test void detailSeriesValidateRepetitionsWeightAndExistingDetail() throws Exception {
        var owner = usuario("TESTER");
        var routine = rutina(owner);
        var exercise = ejercicios.saveAndFlush(new Ejercicios("Serie validacion", null, null, null));
        String jwt = token(owner);
        var relation = request("POST", "/rutina-ejercicios",
                "{\"idRutina\":" + routine.getIdRutina() + ",\"idEjercicio\":" + exercise.getIdEjercicio()
                        + ",\"series\":3,\"repeticiones\":10,\"descansoSeg\":60}", jwt);
        assertEquals(201, relation.statusCode(), relation.body());
        var started = request("POST", "/sesiones/rutina/" + routine.getIdRutina() + "/iniciar", null, jwt);
        assertEquals(201, started.statusCode(), started.body());
        int sessionId = json.readTree(started.body()).get("idSesion").asInt();
        DetalleSesion detail = detallesSesion.findAll().stream()
                .filter(d -> d.getSesion().getIdSesion().equals(sessionId))
                .filter(d -> d.getEjercicio().getIdEjercicio().equals(exercise.getIdEjercicio()))
                .findFirst().orElseThrow();
        assertEquals(1, detallesSesion.findAll().stream()
                .filter(d -> d.getSesion().getIdSesion().equals(sessionId))
                .filter(d -> d.getEjercicio().getIdEjercicio().equals(exercise.getIdEjercicio()))
                .count());
        long before = series.count();
        for (String body : List.of(
                "{\"idDetalle\":" + detail.getIdDetalle() + ",\"numeroSerie\":1,\"repeticiones\":0,\"pesoKg\":1}",
                "{\"idDetalle\":" + detail.getIdDetalle() + ",\"numeroSerie\":1,\"repeticiones\":1,\"pesoKg\":-1}",
                "{\"idDetalle\":" + detail.getIdDetalle() + ",\"numeroSerie\":1,\"repeticiones\":null,\"pesoKg\":1}",
                "{\"idDetalle\":" + detail.getIdDetalle() + ",\"numeroSerie\":1,\"repeticiones\":1,\"pesoKg\":null}",
                "{\"idDetalle\":" + detail.getIdDetalle() + ",\"numeroSerie\":1,\"repeticiones\":1,\"pesoKg\":10000}",
                "{\"idDetalle\":" + detail.getIdDetalle() + ",\"numeroSerie\":1,\"repeticiones\":1,\"pesoKg\":1.001}",
                "{\"idDetalle\":2147483647,\"numeroSerie\":1,\"repeticiones\":1,\"pesoKg\":1}")) {
            var response = request("POST", "/detalle-serie", body, jwt);
            assertTrue(response.statusCode() == 400 || response.statusCode() == 404, response.body());
            assertEquals(before, series.count());
        }
    }

    @Test void publicRegistrationCannotCreateProgramadorNorUnknownRoles() throws Exception {
        String base = "{\"nombres\":\"Ana\",\"apellidos\":\"Test\",\"correo\":\"%s\",\"password\":\"" + PASSWORD
                + "\",\"rol\":\"%s\"}";
        String correo = UUID.randomUUID() + "@example.test";
        // sin token no se puede crear un PROGRAMADOR
        assertEquals(403, request("POST", "/usuarios", base.formatted(correo, "PROGRAMADOR"), null).statusCode());
        assertTrue(usuarios.findByCorreo(correo).isEmpty());
        // los roles que ya no existen se rechazan
        assertEquals(400, request("POST", "/usuarios", base.formatted(correo, "ADMIN"), null).statusCode());
        assertEquals(400, request("POST", "/usuarios", base.formatted(correo, "USUARIA"), null).statusCode());
        // un PROGRAMADOR autenticado sí puede crear otro PROGRAMADOR
        String jwt = token(usuario("PROGRAMADOR"));
        assertEquals(201, request("POST", "/usuarios", base.formatted(correo, "PROGRAMADOR"), jwt).statusCode());
        assertEquals("PROGRAMADOR", usuarios.findByCorreo(correo).orElseThrow().getRol());
    }

    @Test void registrationCanLoginAndPasswordsAreHashed() throws Exception {
        String correo = UUID.randomUUID() + "@example.test";
        String body = "{\"nombres\":\"Ana\",\"apellidos\":\"Test\",\"correo\":\"" + correo
                + "\",\"password\":\"" + PASSWORD + "\"}";
        var registered = request("POST", "/usuarios", body, null);
        assertEquals(201, registered.statusCode(), registered.body());
        assertFalse(registered.body().contains(PASSWORD));
        Usuarios u = usuarios.findByCorreo(correo).orElseThrow();
        assertEquals("TESTER", u.getRol());
        assertNotEquals(PASSWORD, u.getPasswordHash());
        assertTrue(encoder.matches(PASSWORD, u.getPasswordHash()));
        String jwt = token(u);
        assertEquals(200, request("GET", "/progreso", null, jwt).statusCode());
        assertEquals(409, request("POST", "/usuarios", body, null).statusCode());
        assertEquals(401, request("POST", "/login", "{\"correo\":\"" + correo
                + "\",\"password\":\"incorrecta\"}", null).statusCode());
    }

    @Test void registrationAcceptsPasswordWithoutSpecialCharacterAndUsesBCrypt() throws Exception {
        String correo = UUID.randomUUID() + "@example.test";
        String password = "Abcdefg1";
        String body = "{\"nombres\":\"Ana\",\"apellidos\":\"Test\",\"correo\":\"" + correo
                + "\",\"password\":\"" + password + "\"}";
        var response = request("POST", "/usuarios", body, null);
        assertEquals(201, response.statusCode(), response.body());
        Usuarios usuario = usuarios.findByCorreo(correo).orElseThrow();
        assertNotEquals(password, usuario.getPasswordHash());
        assertTrue(encoder.matches(password, usuario.getPasswordHash()));
    }

    @Test void catalogChangesRequireExistingAdministrativeRoles() throws Exception {
        var e = ejercicios.saveAndFlush(new Ejercicios("Original", "Piernas", "Fuerza", "Descripcion"));
        String path = "/ejercicios/" + e.getIdEjercicio();
        String body = "{\"nombre\":\"Actualizado\"}";
        assertEquals(401, request("PUT", path, body, null).statusCode());
        for (String rol : List.of("TESTER")) {
            String jwt = token(usuario(rol));
            assertEquals(403, request("PUT", path, body, jwt).statusCode());
            assertEquals(403, request("DELETE", path, null, jwt).statusCode());
        }
        for (String rol : List.of("PROGRAMADOR")) {
            assertEquals(200, request("PUT", path, body, token(usuario(rol))).statusCode());
        }
        assertEquals(204, request("DELETE", path, null, token(usuario("PROGRAMADOR"))).statusCode());
    }

    @Test void searchFiltersEscapesAndPaginates() throws Exception {
        String marker = UUID.randomUUID().toString();
        ejercicios.saveAndFlush(new Ejercicios(marker + " A", null, "Fuerza", null));
        ejercicios.saveAndFlush(new Ejercicios(marker + " B", null, "fuerza", null));
        ejercicios.saveAndFlush(new Ejercicios(marker + " C", null, "Cardio", null));
        ejercicios.saveAndFlush(new Ejercicios(marker + " %_!", null, "Fuerza", null));
        String jwt = token(usuario("TESTER"));
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

    @Test void exerciseSearchFindsKeywordAloneAndFiltersMuscleGroupCaseInsensitively() throws Exception {
        String marker = "US34-" + UUID.randomUUID();
        String muscleGroup = "Pectoral-" + UUID.randomUUID();
        ejercicios.saveAndFlush(new Ejercicios(marker + " Press", muscleGroup, "Fuerza", null));
        ejercicios.saveAndFlush(new Ejercicios(marker + " Fly", muscleGroup.toUpperCase(Locale.ROOT), "Fuerza", null));
        ejercicios.saveAndFlush(new Ejercicios(marker + " Row", "Espalda-" + UUID.randomUUID(), "Fuerza", null));
        String jwt = token(usuario("TESTER"));

        var keywordResponse = request("GET", "/ejercicios/buscar?query=" + marker, null, jwt);
        assertEquals(200, keywordResponse.statusCode(), keywordResponse.body());
        JsonNode keywordResults = json.readTree(keywordResponse.body());
        assertEquals(3, keywordResults.get("totalElements").asInt());
        for (JsonNode exercise : keywordResults.get("content")) {
            assertTrue(exercise.get("nombre").asText().startsWith(marker));
        }

        var groupResponse = request("GET", "/ejercicios/grupo-muscular/"
                + muscleGroup.toUpperCase(Locale.ROOT), null, jwt);
        assertEquals(200, groupResponse.statusCode(), groupResponse.body());
        JsonNode groupResults = json.readTree(groupResponse.body());
        assertEquals(2, groupResults.size());
        for (JsonNode exercise : groupResults) {
            assertTrue(exercise.get("grupoMuscular").asText().equalsIgnoreCase(muscleGroup));
            assertTrue(exercise.get("nombre").asText().startsWith(marker));
        }
    }

    @Test void exerciseLimitsAndDescriptionPayloadsAreValidated() throws Exception {
        String jwt = token(usuario("PROGRAMADOR"));
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
        var u = usuario("TESTER");
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
                + ",\"nivelEnergia\":3}", token(usuario("TESTER"))).statusCode());
    }

    @Test void concurrentDailyRequestsForDifferentCyclesDoNotDuplicateDate() throws Exception {
        var u = usuario("TESTER");
        var c1 = ciclo(u);
        var c2 = ciclo(u);
        runConcurrently(u, () -> diarioService.guardar(new DetalleDiarioCicloDTO(null,
                        c1.getIdCiclo(), LocalDate.now(), "Menstrual", 3, "uno")),
                () -> diarioService.guardar(new DetalleDiarioCicloDTO(null,
                        c2.getIdCiclo(), LocalDate.now(), "Folicular", 4, "dos")));
        assertEquals(1, diarios.buscarPorUsuarioYFecha(u.getIdUsuario().longValue(), LocalDate.now()).size());
    }

    @Test void dailyObservationsAreOptionalAndRespectDatabaseLimit() throws Exception {
        var u = usuario("TESTER");
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
        assertTrue(tooLong.body().contains("Observaciones"));
        assertTrue(tooLong.body().contains("255"));
        assertEquals(observations, diarios.buscarPorUsuarioYFecha(u.getIdUsuario().longValue(), LocalDate.now())
                .get(0).getObservaciones());
    }

    @Test void initialProfileRejectsInvalidFieldsWithoutPersistence() throws Exception {
        var owner = usuario("TESTER");
        String jwt = token(owner);
        var valid = new LinkedHashMap<String, Object>(Map.of(
                "nivelEntrenamiento", "Inicial", "objetivoPrincipal", "Entrenar",
                "diasDisponibles", 3, "tiempoDisponible", 30, "fechaNacimiento", "2000-01-01"));
        assertEquals(401, request("POST", "/perfiles", json.writeValueAsString(valid), null).statusCode());
        for (String field : valid.keySet()) {
            List<Object> invalids = switch (field) {
                case "diasDisponibles" -> Arrays.asList(null, 0, 8);
                case "tiempoDisponible" -> Arrays.asList(null, 0, -1);
                case "fechaNacimiento" -> Arrays.asList(null, LocalDate.now().plusDays(1).toString());
                default -> Arrays.asList(null, "", "   ");
            };
            for (Object value : invalids) {
                var body = new LinkedHashMap<>(valid);
                body.put(field, value);
                var response = request("POST", "/perfiles", json.writeValueAsString(body), jwt);
                assertEquals(400, response.statusCode(), response.body());
                assertTrue(response.body().toLowerCase().contains(field.toLowerCase()), response.body());
                assertTrue(perfiles.findByUsuario_IdUsuario(owner.getIdUsuario()).isEmpty());
            }
        }
    }

    @Test void profileUpdatesPersistOnlyOwnFieldsAndRejectInvalidChanges() throws Exception {
        var owner = usuario("TESTER");
        var other = usuario("TESTER");
        var original = perfiles.saveAndFlush(new PerfilEntrenamiento(owner, "Inicial",
                "Entrenar", 3, 30, LocalDate.of(2000, 1, 1)));
        var foreign = perfiles.saveAndFlush(new PerfilEntrenamiento(other, "Inicial",
                "Ajeno", 2, 20, LocalDate.of(1999, 1, 1)));
        String jwt = token(owner);
        var profile = new LinkedHashMap<String, Object>(Map.of("nivelEntrenamiento", "Avanzado",
                "objetivoPrincipal", "Fuerza", "idUsuario", other.getIdUsuario(), "idPerfil", foreign.getIdPerfil()));
        assertEquals(200, request("PUT", "/perfiles", json.writeValueAsString(profile), jwt).statusCode());
        for (String field : List.of("nivelEntrenamiento", "objetivoPrincipal")) {
            for (Object value : Arrays.asList(null, "", "   ", "X".repeat(field.equals("nivelEntrenamiento") ? 51 : 101))) {
                var invalid = new LinkedHashMap<>(profile);
                invalid.put(field, value);
                assertEquals(400, request("PUT", "/perfiles", json.writeValueAsString(invalid), jwt).statusCode());
            }
        }
        for (int days : List.of(1, 7)) {
            assertEquals(200, request("PATCH", "/perfiles/disponibilidad",
                    json.writeValueAsString(Map.of("diasDisponibles", days, "tiempoDisponible", 1,
                            "idUsuario", other.getIdUsuario())), jwt).statusCode());
            assertEquals(days, perfiles.findById(original.getIdPerfil()).orElseThrow().getDiasDisponibles());
        }
        for (String field : List.of("diasDisponibles", "tiempoDisponible")) {
            for (Object value : field.equals("diasDisponibles") ? Arrays.asList(null, 0, 8) : Arrays.asList(null, 0, -1)) {
                var invalid = new LinkedHashMap<String, Object>(Map.of("diasDisponibles", 3, "tiempoDisponible", 30));
                invalid.put(field, value);
                assertEquals(400, request("PATCH", "/perfiles/disponibilidad", json.writeValueAsString(invalid), jwt).statusCode());
            }
        }
        var saved = perfiles.findById(original.getIdPerfil()).orElseThrow();
        assertEquals("Avanzado", saved.getNivelEntrenamiento());
        assertEquals("Fuerza", saved.getObjetivoPrincipal());
        assertEquals(7, saved.getDiasDisponibles());
        assertEquals(1, saved.getTiempoDisponible());
        assertEquals(original.getFechaNacimiento(), saved.getFechaNacimiento());
        var unchanged = perfiles.findById(foreign.getIdPerfil()).orElseThrow();
        assertEquals("Ajeno", unchanged.getObjetivoPrincipal());
        assertEquals(2, unchanged.getDiasDisponibles());
        assertEquals(20, unchanged.getTiempoDisponible());
    }

    @Test void progressOptionalFieldsPersistAndInvalidInputsNeverPersist() throws Exception {
        var owner = usuario("TESTER");
        String jwt = token(owner);
        long before = progresos.count();
        assertEquals(401, request("POST", "/progreso", "{\"pesoKg\":60}", null).statusCode());
        for (String field : List.of("pesoKg", "medidaOpcional", "fecha", "notaPersonal")) {
            List<Object> values = switch (field) {
                case "pesoKg" -> Arrays.asList(null, 0, -1, 10000, new BigDecimal("1.001"));
                case "medidaOpcional" -> List.of(0, -1, 10000, new BigDecimal("1.001"));
                case "fecha" -> List.of(LocalDate.now().plusDays(1).toString(), "2026-02-30", "invalid");
                default -> List.of("N".repeat(256));
            };
            for (Object value : values) {
                var body = new LinkedHashMap<String, Object>(Map.of("pesoKg", 60));
                body.put(field, value);
                var response = request("POST", "/progreso", json.writeValueAsString(body), jwt);
                assertEquals(400, response.statusCode(), response.body());
                assertEquals(before, progresos.count());
            }
        }
        for (boolean optional : List.of(false, true)) {
            var body = new LinkedHashMap<String, Object>(Map.of("pesoKg", new BigDecimal("60.25")));
            if (optional) {
                body.put("medidaOpcional", new BigDecimal("80.50"));
                body.put("notaPersonal", "N".repeat(255));
                body.put("fecha", LocalDate.now().minusDays(1).toString());
            }
            var response = request("POST", "/progreso", json.writeValueAsString(body), jwt);
            assertEquals(201, response.statusCode(), response.body());
            var saved = progresos.findById(json.readTree(response.body()).get("idProgreso").asInt()).orElseThrow();
            assertEquals(owner.getIdUsuario(), saved.getUsuario().getIdUsuario());
            assertEquals(new BigDecimal("60.25"), saved.getPesoKg());
            assertEquals(optional ? new BigDecimal("80.50") : null, saved.getMedidaOpcional());
            assertEquals(optional ? "N".repeat(255) : null, saved.getNotaPersonal());
            assertEquals(LocalDate.now().minusDays(optional ? 1 : 0), saved.getFecha());
        }
    }

    @Test void pastDailyUpsertIsPrivateAndEnergyErrorsAreDescriptive() throws Exception {
        var owner = usuario("TESTER");
        var other = usuario("TESTER");
        var ownCycle = ciclo(owner);
        var otherCycle = ciclo(other);
        LocalDate date = LocalDate.now().minusDays(1);
        String jwt = token(owner);
        var body = new LinkedHashMap<String, Object>(Map.of("idCiclo", ownCycle.getIdCiclo(),
                "fecha", date.toString(), "nivelEnergia", 1));
        var created = request("PUT", "/detalle-diario", json.writeValueAsString(body), jwt);
        assertEquals(200, created.statusCode(), created.body());
        int id = json.readTree(created.body()).get("idDetalleDiarioCiclo").asInt();
        body.put("nivelEnergia", 5);
        assertEquals(200, request("PUT", "/detalle-diario", json.writeValueAsString(body), jwt).statusCode());
        for (Object value : Arrays.asList(null, 0, 6)) {
            body.put("nivelEnergia", value);
            var invalid = request("PUT", "/detalle-diario", json.writeValueAsString(body), jwt);
            assertEquals(400, invalid.statusCode(), invalid.body());
            assertTrue(invalid.body().contains("NivelEnergia"), invalid.body());
            assertEquals(5, diarios.findById(id).orElseThrow().getNivelEnergia());
        }
        body.put("nivelEnergia", "1.5");
        var invalidConversion = request("PUT", "/detalle-diario", json.writeValueAsString(body), jwt);
        assertEquals(400, invalidConversion.statusCode(), invalidConversion.body());
        assertEquals(5, diarios.findById(id).orElseThrow().getNivelEnergia());
        body.put("nivelEnergia", 1);
        body.put("idCiclo", otherCycle.getIdCiclo());
        assertEquals(200, request("PUT", "/detalle-diario", json.writeValueAsString(body), token(other)).statusCode());
        assertEquals(403, request("PUT", "/detalle-diario", json.writeValueAsString(body), jwt).statusCode());
        var saved = diarios.buscarPorUsuarioYFecha(owner.getIdUsuario().longValue(), date);
        assertEquals(1, saved.size());
        assertEquals(id, saved.get(0).getIdDetalleDiarioCiclo());
        assertEquals(5, saved.get(0).getNivelEnergia());
        assertEquals(1, diarios.buscarPorUsuarioYFecha(other.getIdUsuario().longValue(), date).size());
    }

    @Test void exerciseResponsesPreserveLongDescriptionAndAllCatalogFields() throws Exception {
        String jwt = token(usuario("PROGRAMADOR"));
        String name = "Long-" + UUID.randomUUID();
        String description = "Movimiento controlado. ".repeat(500);
        var response = request("POST", "/ejercicios", json.writeValueAsString(Map.of(
                "nombre", name, "grupoMuscular", "Piernas", "tipo", "Fuerza", "descripcion", description)), jwt);
        assertEquals(201, response.statusCode(), response.body());
        int id = json.readTree(response.body()).get("idEjercicio").asInt();
        assertEquals(description, ejercicios.findById(id).orElseThrow().getDescripcion());
        for (String path : List.of("/ejercicios/" + id, "/ejercicios/buscar?query=" + name)) {
            var fetched = request("GET", path, null, jwt);
            assertEquals(200, fetched.statusCode(), fetched.body());
            var item = json.readTree(fetched.body());
            if (path.contains("buscar")) item = item.get("content").get(0);
            assertEquals(name, item.get("nombre").asText());
            assertEquals("Piernas", item.get("grupoMuscular").asText());
            assertEquals("Fuerza", item.get("tipo").asText());
            assertEquals(description, item.get("descripcion").asText());
        }
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
        var u = usuario("TESTER");
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
        assertTrue(perfiles.findByUsuario_IdUsuario(u.getIdUsuario()).isPresent());
        assertThrows(DataIntegrityViolationException.class, () -> perfiles.saveAndFlush(
                new PerfilEntrenamiento(u, "Inicial", "Entrenar", 3, 30, LocalDate.of(2000, 1, 1))));
    }

    @Test void sessionPostAndPutEnforceOwnershipAndSameValidation() throws Exception {
        var owner = usuario("TESTER");
        var other = usuario("TESTER");
        var ownRoutine = rutina(owner);
        var otherRoutine = rutina(other);
        String jwt = token(owner);
        String otherJwt = token(other);

        var started = request("POST", "/sesiones/rutina/" + ownRoutine.getIdRutina() + "/iniciar", null, jwt);
        assertEquals(201, started.statusCode(), started.body());
        int id = json.readTree(started.body()).get("idSesion").asInt();
        String path = "/sesiones/" + id;

        assertEquals(405, request("POST", "/sesiones", "{\"idRutina\":" + ownRoutine.getIdRutina() + "}", jwt).statusCode());
        assertEquals(405, request("PUT", path, "{\"duracionMin\":30,\"nivelEnergia\":3,\"esfuerzoPercibido\":5}", jwt).statusCode());
        assertEquals(405, request("DELETE", path, null, jwt).statusCode());

        assertEquals(403, request("GET", path, null, otherJwt).statusCode());
        assertEquals(403, request("GET", "/sesiones/usuario/" + owner.getIdUsuario(), null, otherJwt).statusCode());
        assertEquals(405, request("PUT", path, "{\"duracionMin\":30,\"nivelEnergia\":3,\"esfuerzoPercibido\":5}", otherJwt).statusCode());
        assertEquals(405, request("DELETE", path, null, otherJwt).statusCode());
        assertEquals(0, json.readTree(request("GET", "/sesiones", null, otherJwt).body()).size());

        var otherStartAttempt = request("POST", "/sesiones/rutina/" + otherRoutine.getIdRutina() + "/iniciar", null, jwt);
        assertEquals(403, otherStartAttempt.statusCode(), otherStartAttempt.body());
        assertEquals("EN_CURSO", sesiones.findById(id).orElseThrow().getEstado());
    }

    @Test void routineOwnershipCannotBeTakenThroughPut() throws Exception {
        var owner = usuario("TESTER");
        var intruder = usuario("TESTER");
        var r = rutina(owner);
        String responseBody = "{\"idUsuario\":" + intruder.getIdUsuario() + ",\"nombre\":\"Robada\"}";
        assertEquals(403, request("PUT", "/rutinas/" + r.getIdRutina(), responseBody, token(intruder)).statusCode());
        assertEquals(owner.getIdUsuario(), rutinas.findById(r.getIdRutina()).orElseThrow().getUsuario().getIdUsuario());
    }

    @Test void recommendationsArePrivateEmptyAndAcceptanceIsIdempotent() throws Exception {
        var u = usuario("TESTER");
        String jwt = token(u);
        assertEquals(0, json.readTree(request("GET", "/recomendaciones", null, jwt).body()).get("totalElements").asInt());
        // Fixture persisted directly: this is not a pretend recommendation generator.
        var e = recomendaciones.saveAndFlush(new RecomendacionesIA(u, null, LocalDateTime.now(),
                "Fixture", "Contenido de prueba", "Motivo de prueba", false));
        LocalDateTime storedDate = recomendaciones.findById(e.getIdRecomendacion()).orElseThrow().getFecha();
        String path = "/recomendaciones/" + e.getIdRecomendacion();
        var other = usuario("TESTER");
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
        var u = usuario("TESTER");
        autenticar(u);
        var r = rutina(u);
        var e = ejercicios.saveAndFlush(new Ejercicios("Referenciado", null, null, null));
        rutinaEjerciciosService.registrar(new RutinaEjerciciosDTO(null, r.getIdRutina(), e.getIdEjercicio(), 3, 10, 30));
        assertEquals(409, request("DELETE", "/ejercicios/" + e.getIdEjercicio(), null, token(usuario("PROGRAMADOR"))).statusCode());
        assertTrue(ejercicios.existsById(e.getIdEjercicio()));
    }

    @Test void historyJoinAndProfileQueriesKeepTheirBehaviorAndReportTimings() {
        var u = usuario("TESTER");
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

    @Test void adminAndUsuariaHaveSeparatePermissions() throws Exception {
        String admin = token(usuario("ADMIN"));
        String usuaria = token(usuario("USUARIA"));
        // ADMIN no accede a los datos personales ni a las funciones de la usuaria.
        for (String path : List.of("/ciclos", "/detalle-diario", "/rutinas", "/sesiones",
                "/progreso", "/perfiles", "/recomendaciones")) {
            assertEquals(403, request("GET", path, null, admin).statusCode(), path);
            assertNotEquals(403, request("GET", path, null, usuaria).statusCode(), path);
        }
        assertEquals(403, request("POST", "/ia/chat", "{}", admin).statusCode());
        // USUARIA no administra usuarios ni modifica el catalogo.
        assertEquals(403, request("GET", "/usuarios", null, usuaria).statusCode());
        assertEquals(403, request("POST", "/ejercicios", "{\"nombre\":\"Nuevo\"}", usuaria).statusCode());
        assertEquals(403, request("POST", "/ejercicios/wger/sincronizar", null, usuaria).statusCode());
        // Ambos roles pueden consultar el catalogo.
        assertEquals(200, request("GET", "/ejercicios/buscar", null, admin).statusCode());
        assertEquals(200, request("GET", "/ejercicios/buscar", null, usuaria).statusCode());
        // Sin token no se accede a nada protegido.
        assertEquals(401, request("GET", "/ciclos", null, null).statusCode());
    }
}
