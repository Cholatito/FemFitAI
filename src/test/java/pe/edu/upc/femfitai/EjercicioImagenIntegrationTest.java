package pe.edu.upc.femfitai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upc.femfitai.entities.Usuarios;
import pe.edu.upc.femfitai.repositories.EjerciciosRepository;
import pe.edu.upc.femfitai.repositories.UsuariosRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import javax.sql.DataSource;
import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.config.location=classpath:/application-test.properties")
class EjercicioImagenIntegrationTest {
    @LocalServerPort int port;
    @Autowired UsuariosRepository usuarios;
    @Autowired EjerciciosRepository ejercicios;
    @Autowired PasswordEncoder encoder;
    @Autowired DataSource dataSource;
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    pe.edu.upc.femfitai.services.implementations.WgerClient wgerClient;
    final HttpClient client = HttpClient.newHttpClient();
    final JsonMapper json = JsonMapper.builder().build();
    String adminToken, clienteToken;
    final String fotografia = "https://imagenes.example.test/sentadilla.jpg";

    @BeforeEach void preparar() throws Exception {
        adminToken = login("ADMIN");
        clienteToken = login("USUARIA");
    }
    String login(String rol) throws Exception {
        String password = "SyntheticPhotoTest123!";
        var u = usuarios.saveAndFlush(new Usuarios("Imagen", "Prueba", UUID.randomUUID()+"@example.test",
                encoder.encode(password), rol, true, LocalDateTime.now()));
        var response = request("POST", "/login", Map.of("correo", u.getCorreo(), "password", password), null);
        assertEquals(200, response.statusCode());
        return json.readTree(response.body()).get("accessToken").asText();
    }
    HttpResponse<String> request(String method, String path, Object datos, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:"+port+path))
                .timeout(Duration.ofSeconds(15)).header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer "+token);
        return client.send(builder.method(method, datos == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(datos))).build(), HttpResponse.BodyHandlers.ofString());
    }
    Map<String, Object> datos() {
        return new HashMap<>(Map.of("nombre", "Sentadilla de prueba", "grupoMuscular", "Piernas", "tipo", "Fuerza",
                "imagenUrl", fotografia, "imagenAutor", "Autor de prueba", "imagenLicencia", "CC BY 4.0",
                "imagenLicenciaUrl", "https://creativecommons.org/licenses/by/4.0/"));
    }
    int crear() throws Exception {
        var r = request("POST", "/ejercicios", datos(), adminToken);
        assertEquals(201, r.statusCode(), r.body());
        JsonNode result = json.readTree(r.body());
        assertFalse(result.has("imagenIncluida"));
        return result.get("idEjercicio").asInt();
    }

    @Test void usaBaseDePruebaEnMemoria() throws Exception {
        try (var c = dataSource.getConnection()) { assertTrue(c.getMetaData().getURL().startsWith("jdbc:h2:mem:")); }
    }
    @Test void guardaImagenYCreditosYLosPublicaEnBusquedaParaCliente() throws Exception {
        int id = crear();
        var stored = ejercicios.findById(id).orElseThrow();
        assertEquals(fotografia, stored.getImagenUrl());
        assertEquals("Autor de prueba", stored.getImagenAutor());
        var r = request("GET", "/ejercicios/"+id, null, clienteToken);
        assertEquals(200, r.statusCode());
        assertEquals(fotografia, json.readTree(r.body()).get("imagenUrl").asText());
        var listado = request("GET", "/ejercicios/buscar?query=Sentadilla&tamano=100", null, clienteToken);
        assertEquals(200, listado.statusCode());
        boolean encontrado = false;
        for (var item : json.readTree(listado.body()).get("content"))
            if (item.get("idEjercicio").asInt() == id) {
                assertEquals(fotografia, item.get("imagenUrl").asText()); encontrado = true;
            }
        assertTrue(encontrado);
    }
    @Test void actualizaFotografiaYCreditosSinCambiarElIdentificador() throws Exception {
        int id = crear();
        var data = datos(); data.put("imagenUrl", "https://imagenes.example.test/sentadilla-2.webp");
        data.put("imagenAutor", "Otro autor");
        var r = request("PUT", "/ejercicios/"+id, data, adminToken);
        assertEquals(200, r.statusCode(), r.body());
        assertEquals(data.get("imagenUrl"), ejercicios.findById(id).orElseThrow().getImagenUrl());
        assertEquals("Otro autor", ejercicios.findById(id).orElseThrow().getImagenAutor());
    }
    @Test void unClienteAntiguoQueNoEnviaImagenConservaLaExistente() throws Exception {
        int id = crear();
        var r = request("PUT", "/ejercicios/"+id, Map.of("nombre", "Nombre actualizado"), adminToken);
        assertEquals(200, r.statusCode(), r.body());
        var stored = ejercicios.findById(id).orElseThrow();
        assertEquals("Nombre actualizado", stored.getNombre());
        assertEquals(fotografia, stored.getImagenUrl());
        assertEquals("Autor de prueba", stored.getImagenAutor());
    }
    @Test void retirarImagenExpresamenteLimpiaTambienSusCreditos() throws Exception {
        int id = crear();
        var data = datos(); data.put("imagenUrl", null);
        var r = request("PUT", "/ejercicios/"+id, data, adminToken);
        assertEquals(200, r.statusCode(), r.body());
        var stored = ejercicios.findById(id).orElseThrow();
        assertNull(stored.getImagenUrl()); assertNull(stored.getImagenAutor());
        assertNull(stored.getImagenLicencia()); assertNull(stored.getImagenLicenciaUrl());
    }
    @Test void rechazaUrlsInvalidasSinPersistirCambiosParciales() throws Exception {
        int id = crear();
        for (String url : List.of("javascript:alert(1)", "data:image/png;base64,AA", "http://example.test/a.jpg",
                "https://usuario:clave@example.test/a.jpg", "https://example.test/a.jpg#fragmento", "https:sin-host")) {
            var data = datos(); data.put("imagenUrl", url); data.put("nombre", "No debe guardarse");
            var r = request("PUT", "/ejercicios/"+id, data, adminToken);
            assertEquals(400, r.statusCode(), url+": "+r.body());
            assertEquals("Sentadilla de prueba", ejercicios.findById(id).orElseThrow().getNombre());
            assertEquals(fotografia, ejercicios.findById(id).orElseThrow().getImagenUrl());
        }
        var data = datos(); data.put("imagenLicenciaUrl", "javascript:alert(1)");
        assertEquals(400, request("PUT", "/ejercicios/"+id, data, adminToken).statusCode());
    }
    @Test void soloAdminPuedeCambiarLaImagenYUnIdInexistenteDevuelve404() throws Exception {
        int id = crear();
        assertEquals(401, request("PUT", "/ejercicios/"+id, datos(), null).statusCode());
        assertEquals(403, request("PUT", "/ejercicios/"+id, datos(), clienteToken).statusCode());
        assertEquals(403, request("POST", "/ejercicios", datos(), clienteToken).statusCode());
        assertEquals(404, request("PUT", "/ejercicios/2147483647", datos(), adminToken).statusCode());
        assertEquals(fotografia, ejercicios.findById(id).orElseThrow().getImagenUrl());
    }

    void simularWger() {
        org.mockito.Mockito.when(wgerClient.obtenerPaginaEjercicios(0)).thenReturn(Map.of("results", List.of(Map.of(
                "id", 48123, "translations", List.of(Map.of("language", 4, "name", "Remo importado de prueba")),
                "images", List.of(Map.of("exercise", 48123, "image", "https://wger.de/media/exercise-images/48123/prueba.png",
                        "license", 2, "license_author", "Autor prueba", "is_main", true, "style", "1"))))));
        org.mockito.Mockito.when(wgerClient.obtenerLicencias()).thenReturn(Map.of("results", List.of(Map.of(
                "id", 2, "short_name", "CC-BY-SA 4", "url", "http://creativecommons.org/licenses/by-sa/4.0/"))));
    }
    @Test void soloAdminSincronizaYClientePuedeConsultarLaImagenImportada() throws Exception {
        assertEquals(401, request("POST", "/ejercicios/wger/sincronizar", Map.of(), null).statusCode());
        assertEquals(403, request("POST", "/ejercicios/wger/sincronizar", Map.of(), clienteToken).statusCode());
        org.mockito.Mockito.verifyNoInteractions(wgerClient);
        ejercicios.findByWgerId(48123).ifPresent(ejercicios::delete);
        simularWger();
        var r = request("POST", "/ejercicios/wger/sincronizar", Map.of(), adminToken);
        assertEquals(200, r.statusCode(), r.body());
        assertEquals(1, json.readTree(r.body()).get("imagenesImportadas").asInt());
        var item = ejercicios.findByWgerId(48123).orElseThrow();
        var lectura = request("GET", "/ejercicios/"+item.getIdEjercicio(), null, clienteToken);
        assertEquals(200, lectura.statusCode());
        assertEquals(item.getImagenUrl(), json.readTree(lectura.body()).get("imagenUrl").asText());
        var segundo = request("POST", "/ejercicios/wger/sincronizar", Map.of(), adminToken);
        assertEquals(200, segundo.statusCode());
        assertEquals(0, json.readTree(segundo.body()).get("imagenesImportadas").asInt());
        assertEquals(item.getIdEjercicio(), ejercicios.findByWgerId(48123).orElseThrow().getIdEjercicio());
    }
    @Test void falloDeLicenciasDevuelve206SinBorrarEjerciciosNiImagenes() throws Exception {
        ejercicios.findByWgerId(48123).ifPresent(ejercicios::delete);
        simularWger();
        org.mockito.Mockito.when(wgerClient.obtenerLicencias()).thenThrow(new IllegalStateException("Fallo simulado"));
        var r = request("POST", "/ejercicios/wger/sincronizar", Map.of(), adminToken);
        assertEquals(206, r.statusCode(), r.body());
        assertFalse(json.readTree(r.body()).get("completo").asBoolean());
        assertTrue(json.readTree(r.body()).get("mensaje").asText().contains("licencias"));
        assertTrue(ejercicios.findByWgerId(48123).isPresent());
    }
}
