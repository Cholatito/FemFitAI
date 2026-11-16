package pe.edu.upc.femfitai.services.implementations;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;
import pe.edu.upc.femfitai.entities.Ejercicios;
import pe.edu.upc.femfitai.repositories.EjerciciosRepository;
import pe.edu.upc.femfitai.services.interfaces.IWgerSyncService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WgerSyncServiceTest {

    @Test
    void seleccionaTraduccionEspanolaCuandoExiste() {
        WgerClient client = mock(WgerClient.class);
        EjerciciosRepository repository = mock(EjerciciosRepository.class);
        WgerSyncService service = new WgerSyncService(client, repository);

        when(client.obtenerPaginaEjercicios(0)).thenReturn(pagina(List.of(
                ejercicio(11, "Sentadilla", List.of(
                                traduccion(4, "Sentadilla", "Descripción en español", "Ana"),
                                traduccion(2, "Squat", "Description in English", "Bob")
                        ),
                        Map.of("name", "Legs")
                )
        )));
        when(repository.findByWgerId(11)).thenReturn(Optional.empty());
        when(repository.save(any(Ejercicios.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IWgerSyncService.ResultadoSincronizacion resumen = service.sincronizarTodos();

        ArgumentCaptor<Ejercicios> captor = ArgumentCaptor.forClass(Ejercicios.class);
        verify(repository).save(captor.capture());
        assertEquals(1, resumen.totalConsultados());
        assertEquals(1, resumen.insertados());
        assertEquals(0, resumen.omitidos());
        assertEquals("Sentadilla", captor.getValue().getNombre());
        assertEquals("Cuádriceps", captor.getValue().getGrupoMuscular());
        assertNull(captor.getValue().getTipo());
        assertTrue(captor.getValue().getDescripcion().contains("Descripción en español"));
        assertTrue(captor.getValue().getDescripcion().contains("Ana"));
    }

    @Test
    void fallbackAInglesCuandoNoHayEspanol() {
        WgerClient client = mock(WgerClient.class);
        EjerciciosRepository repository = mock(EjerciciosRepository.class);
        WgerSyncService service = new WgerSyncService(client, repository);

        when(client.obtenerPaginaEjercicios(0)).thenReturn(pagina(List.of(
                ejercicio(12, "Bench Press", List.of(
                                traduccion(2, "Bench Press", "Pressing movement", "Author 2")
                        ),
                        Map.of("name", "Arms")
                )
        )));
        when(repository.findByWgerId(12)).thenReturn(Optional.empty());
        when(repository.save(any(Ejercicios.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IWgerSyncService.ResultadoSincronizacion resumen = service.sincronizarTodos();

        ArgumentCaptor<Ejercicios> captor = ArgumentCaptor.forClass(Ejercicios.class);
        verify(repository).save(captor.capture());
        assertEquals("Bench Press", captor.getValue().getNombre());
        assertEquals("Bíceps", captor.getValue().getGrupoMuscular());
        assertTrue(captor.getValue().getDescripcion().contains("Pressing movement"));
        assertEquals(1, resumen.insertados());
    }

    @Test
    void bicepsFemorisNoSeClasificaComoBrazos() {
        WgerClient client = mock(WgerClient.class);
        EjerciciosRepository repository = mock(EjerciciosRepository.class);
        WgerSyncService service = new WgerSyncService(client, repository);

        Map<String, Object> ejercicioBicepsFemoris = new java.util.LinkedHashMap<>();
        ejercicioBicepsFemoris.put("id", 40);
        ejercicioBicepsFemoris.put("translations", List.of(traduccion(4, "Hamstring Curl", "Descripcion", "Autor")));
        ejercicioBicepsFemoris.put("muscles", List.of(Map.of("name", "Biceps femoris")));
        ejercicioBicepsFemoris.put("muscles_secondary", List.of());
        ejercicioBicepsFemoris.put("category", Map.of("name", "Legs"));
        ejercicioBicepsFemoris.put("license", Map.of("full_name", "CC BY-SA 3.0", "name", "CC BY-SA 3.0", "url", "https://example.com/license"));
        ejercicioBicepsFemoris.put("license_author", "Wger");

        when(client.obtenerPaginaEjercicios(0)).thenReturn(pagina(List.of(ejercicioBicepsFemoris)));
        when(repository.findByWgerId(40)).thenReturn(Optional.empty());
        when(repository.save(any(Ejercicios.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.sincronizarTodos();

        ArgumentCaptor<Ejercicios> captor = ArgumentCaptor.forClass(Ejercicios.class);
        verify(repository).save(captor.capture());
        assertEquals("Isquiotibiales", captor.getValue().getGrupoMuscular());
    }

    @Test
    void armsYAbsSeTraducenCorrectamente() {
        WgerClient client = mock(WgerClient.class);
        EjerciciosRepository repository = mock(EjerciciosRepository.class);
        WgerSyncService service = new WgerSyncService(client, repository);

        Map<String, Object> ejercicioArms = new java.util.LinkedHashMap<>();
        ejercicioArms.put("id", 42);
        ejercicioArms.put("translations", List.of(traduccion(4, "Curl", "Desc", "Autor")));
        ejercicioArms.put("muscles", List.of(Map.of("name", "Arms")));
        ejercicioArms.put("muscles_secondary", List.of());
        ejercicioArms.put("category", Map.of("name", "Arms"));
        ejercicioArms.put("license", Map.of("full_name", "CC BY-SA 3.0", "name", "CC BY-SA 3.0", "url", "https://example.com/license"));
        ejercicioArms.put("license_author", "Wger");

        Map<String, Object> ejercicioAbs = new java.util.LinkedHashMap<>();
        ejercicioAbs.put("id", 43);
        ejercicioAbs.put("translations", List.of(traduccion(4, "Crunch", "Desc", "Autor")));
        ejercicioAbs.put("muscles", List.of(Map.of("name", "Abs")));
        ejercicioAbs.put("muscles_secondary", List.of());
        ejercicioAbs.put("category", Map.of("name", "Abs"));
        ejercicioAbs.put("license", Map.of("full_name", "CC BY-SA 3.0", "name", "CC BY-SA 3.0", "url", "https://example.com/license"));
        ejercicioAbs.put("license_author", "Wger");

        when(client.obtenerPaginaEjercicios(0)).thenReturn(pagina(List.of(ejercicioArms, ejercicioAbs)));
        when(repository.findByWgerId(any(Integer.class))).thenReturn(Optional.empty());
        when(repository.save(any(Ejercicios.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.sincronizarTodos();

        ArgumentCaptor<Ejercicios> captor = ArgumentCaptor.forClass(Ejercicios.class);
        verify(repository, times(2)).save(captor.capture());
        List<Ejercicios> guardados = captor.getAllValues();
        assertTrue(guardados.stream().anyMatch(e -> e.getGrupoMuscular().equals("Brazos") && e.getNombre().equals("Curl")));
        assertTrue(guardados.stream().anyMatch(e -> e.getGrupoMuscular().equals("Abdominales") && e.getNombre().equals("Crunch")));
    }

    @Test
    void quadricepsFemorisSeClasificaCorrectamente() {
        WgerClient client = mock(WgerClient.class);
        EjerciciosRepository repository = mock(EjerciciosRepository.class);
        WgerSyncService service = new WgerSyncService(client, repository);

        Map<String, Object> ejercicioQuadriceps = new java.util.LinkedHashMap<>();
        ejercicioQuadriceps.put("id", 41);
        ejercicioQuadriceps.put("translations", List.of(traduccion(4, "Leg Extension", "Descripcion", "Autor")));
        ejercicioQuadriceps.put("muscles", List.of(Map.of("name", "Quadriceps femoris")));
        ejercicioQuadriceps.put("muscles_secondary", List.of());
        ejercicioQuadriceps.put("category", Map.of("name", "Legs"));
        ejercicioQuadriceps.put("license", Map.of("full_name", "CC BY-SA 3.0", "name", "CC BY-SA 3.0", "url", "https://example.com/license"));
        ejercicioQuadriceps.put("license_author", "Wger");

        when(client.obtenerPaginaEjercicios(0)).thenReturn(pagina(List.of(ejercicioQuadriceps)));
        when(repository.findByWgerId(41)).thenReturn(Optional.empty());
        when(repository.save(any(Ejercicios.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.sincronizarTodos();

        ArgumentCaptor<Ejercicios> captor = ArgumentCaptor.forClass(Ejercicios.class);
        verify(repository).save(captor.capture());
        assertEquals("Cuádriceps", captor.getValue().getGrupoMuscular());
    }

    @Test
    void conservaCaracteresEspecialesEnDescripcionYAutores() {
        WgerClient client = mock(WgerClient.class);
        EjerciciosRepository repository = mock(EjerciciosRepository.class);
        WgerSyncService service = new WgerSyncService(client, repository);

        Map<String, Object> ejercicio = new java.util.LinkedHashMap<>();
        ejercicio.put("id", 44);
        ejercicio.put("translations", List.of(
                Map.of("language", 4, "name", "Sentadilla", "description_source", "Si&eacute;ntate, levanta el &aacute;ngulo y repite con ni&ntilde;o.", "license_author", "Ana")));
        ejercicio.put("muscles", List.of(Map.of("name", "Legs")));
        ejercicio.put("muscles_secondary", List.of());
        ejercicio.put("category", Map.of("name", "Legs"));
        ejercicio.put("license", Map.of("full_name", "CC BY-SA 3.0", "name", "CC BY-SA 3.0", "url", "https://example.com/license"));
        ejercicio.put("license_author", "Wger");

        when(client.obtenerPaginaEjercicios(0)).thenReturn(pagina(List.of(ejercicio)));
        when(repository.findByWgerId(44)).thenReturn(Optional.empty());
        when(repository.save(any(Ejercicios.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IWgerSyncService.ResultadoSincronizacion resumen = service.sincronizarTodos();

        ArgumentCaptor<Ejercicios> captor = ArgumentCaptor.forClass(Ejercicios.class);
        verify(repository).save(captor.capture());
        assertTrue(captor.getValue().getDescripcion().contains("Siéntate, levanta el ángulo y repite con niño."));
        assertTrue(captor.getValue().getDescripcion().contains("niño"));
        assertTrue(captor.getValue().getDescripcion().contains("Ana"));
        assertTrue(captor.getValue().getDescripcion().contains("CC BY-SA 3.0"));
        assertEquals(1, resumen.insertados());
    }

    @Test
    void manejaTraduccionesVaciasYLasOmite() {
        WgerClient client = mock(WgerClient.class);
        EjerciciosRepository repository = mock(EjerciciosRepository.class);
        WgerSyncService service = new WgerSyncService(client, repository);

        when(client.obtenerPaginaEjercicios(0)).thenReturn(pagina(List.of(
                ejercicio(13, null, List.of(
                                traduccion(4, "   ", "   ", "")
                        ),
                        Map.of("name", "Chest")
                )
        )));
        when(repository.findByWgerId(13)).thenReturn(Optional.empty());

        IWgerSyncService.ResultadoSincronizacion resumen = service.sincronizarTodos();

        verify(repository, never()).save(any(Ejercicios.class));
        assertEquals(1, resumen.totalConsultados());
        assertEquals(0, resumen.insertados());
        assertEquals(1, resumen.omitidos());
    }

    @Test
    void obtieneVariasPaginas() {
        WgerClient client = mock(WgerClient.class);
        EjerciciosRepository repository = mock(EjerciciosRepository.class);
        WgerSyncService service = new WgerSyncService(client, repository);

        when(client.obtenerPaginaEjercicios(0)).thenReturn(pagina(List.of(
                ejercicio(21, "Ejercicio 1", List.of(traduccion(4, "Ejercicio 1", "Desc 1", "Autor 1")), Map.of("name", "Back")),
                ejercicio(22, "Ejercicio 2", List.of(traduccion(4, "Ejercicio 2", "Desc 2", "Autor 2")), Map.of("name", "Legs"))
        ), "https://wger.de/api/v2/exerciseinfo/?offset=20"));
        when(client.obtenerPaginaEjercicios(20)).thenReturn(pagina(List.of(
                ejercicio(23, "Ejercicio 3", List.of(traduccion(4, "Ejercicio 3", "Desc 3", "Autor 3")), Map.of("name", "Shoulders"))
        )));
        when(repository.findByWgerId(any(Integer.class))).thenReturn(Optional.empty());
        when(repository.save(any(Ejercicios.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IWgerSyncService.ResultadoSincronizacion resumen = service.sincronizarTodos();

        verify(repository, times(3)).save(any(Ejercicios.class));
        assertEquals(3, resumen.totalConsultados());
        assertEquals(3, resumen.insertados());
        assertEquals(0, resumen.omitidos());
    }

    @Test
    void actualizaEjerciciosExistentesSinCambiarIdEjercicio() {
        WgerClient client = mock(WgerClient.class);
        EjerciciosRepository repository = mock(EjerciciosRepository.class);
        WgerSyncService service = new WgerSyncService(client, repository);

        Ejercicios existente = new Ejercicios("Viejo nombre", "Pecho", null, "Vieja descripcion");
        existente.setIdEjercicio(99);
        existente.setWgerId(77);

        when(client.obtenerPaginaEjercicios(0)).thenReturn(pagina(List.of(
                ejercicio(77, "Nuevo nombre", List.of(traduccion(4, "Nuevo nombre", "Nueva descripcion", "Autor")), Map.of("name", "Legs"))
        )));
        when(repository.findByWgerId(77)).thenReturn(Optional.of(existente));
        when(repository.save(any(Ejercicios.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IWgerSyncService.ResultadoSincronizacion resumen = service.sincronizarTodos();

        ArgumentCaptor<Ejercicios> captor = ArgumentCaptor.forClass(Ejercicios.class);
        verify(repository).save(captor.capture());
        assertEquals(99, captor.getValue().getIdEjercicio());
        assertEquals("Nuevo nombre", captor.getValue().getNombre());
        assertEquals("Cuádriceps", captor.getValue().getGrupoMuscular());
        assertTrue(captor.getValue().getDescripcion().contains("Nueva descripcion"));
        assertTrue(captor.getValue().getDescripcion().contains("Autor: Wger, Autor"));
        assertEquals(1, resumen.actualizados());
    }

    @Test
    void noModificaEjerciciosManualesConWgerIdNulo() {
        WgerClient client = mock(WgerClient.class);
        EjerciciosRepository repository = mock(EjerciciosRepository.class);
        WgerSyncService service = new WgerSyncService(client, repository);

        when(client.obtenerPaginaEjercicios(0)).thenReturn(pagina(List.of(
                Map.of("translations", List.of(Map.of("language", 4, "name", "Manual", "description_source", "Desc")), "muscles", List.of(), "category", Map.of("name", "Chest"), "id", 999, "license", Map.of("full_name", "CC BY-SA 3.0", "name", "CC BY-SA 3.0"))
        )));
        when(repository.findByWgerId(any(Integer.class))).thenReturn(Optional.empty());
        when(repository.save(any(Ejercicios.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IWgerSyncService.ResultadoSincronizacion resumen = service.sincronizarTodos();

        assertEquals(1, resumen.totalConsultados());
        assertEquals(1, resumen.insertados());
        assertEquals(0, resumen.omitidos());
        verify(repository, times(1)).save(any(Ejercicios.class));
    }

    @Test
    void manejaErroresDeLaApiExternaSinRomperLaSincronizacion() {
        WgerClient client = mock(WgerClient.class);
        EjerciciosRepository repository = mock(EjerciciosRepository.class);
        WgerSyncService service = new WgerSyncService(client, repository);

        when(client.obtenerPaginaEjercicios(0)).thenThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "wger caído"));

        IWgerSyncService.ResultadoSincronizacion resumen = service.sincronizarTodos();

        assertEquals(0, resumen.totalConsultados());
        assertEquals(0, resumen.insertados());
        assertEquals(0, resumen.omitidos());
        assertFalse(resumen.completo());
        assertEquals(0, resumen.offsetError());
        verify(repository, never()).save(any(Ejercicios.class));
    }

    @Test
    void noGeneraDuplicadosAlEjecutarLaSincronizacionVariasVeces() {
        WgerClient client = mock(WgerClient.class);
        EjerciciosRepository repository = mock(EjerciciosRepository.class);
        WgerSyncService service = new WgerSyncService(client, repository);

        when(client.obtenerPaginaEjercicios(0)).thenReturn(pagina(List.of(
                ejercicio(12, "Exercise", List.of(traduccion(4, "Exercise", "Desc", "Autor")), Map.of("name", "Legs"))
        )));
        when(repository.findByWgerId(12)).thenReturn(Optional.empty(), Optional.of(new Ejercicios("Exercise", "Piernas", null, "Desc")));
        when(repository.save(any(Ejercicios.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IWgerSyncService.ResultadoSincronizacion primer = service.sincronizarTodos();
        IWgerSyncService.ResultadoSincronizacion segundo = service.sincronizarTodos();

        assertEquals(1, primer.insertados());
        assertEquals(0, segundo.insertados());
        verify(repository, times(1)).save(any(Ejercicios.class));
    }

    private static Map<String, Object> pagina(List<Map<String, Object>> results) {
        return pagina(results, null);
    }

    private static Map<String, Object> pagina(List<Map<String, Object>> results, String next) {
        Map<String, Object> page = new java.util.LinkedHashMap<>();
        page.put("count", results.size());
        page.put("next", next);
        page.put("previous", null);
        page.put("results", results);
        return page;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> ejercicio(Integer id, String nombre, List<Map<String, Object>> traducciones, Map<String, String> categoria) {
        Map<String, Object> item = new java.util.LinkedHashMap<>();
        item.put("id", id);
        item.put("name", nombre);
        item.put("translations", traducciones);
        item.put("muscles", List.of(Map.of("name", principalMusculo(categoria))));
        item.put("muscles_secondary", List.of());
        item.put("category", categoria);
        item.put("license", Map.of("full_name", "CC BY-SA 3.0", "name", "CC BY-SA 3.0"));
        item.put("license_author", "Wger");
        return item;
    }

    private static String principalMusculo(Map<String, String> categoria) {
        if (categoria == null) {
            return "Biceps brachii";
        }
        String nombreCategoria = categoria.get("name");
        if (nombreCategoria == null) {
            return "Biceps brachii";
        }
        String categoriaNormalizada = nombreCategoria.toLowerCase();
        if (categoriaNormalizada.contains("leg") || categoriaNormalizada.contains("pierna")) {
            return "Quadriceps femoris";
        }
        if (categoriaNormalizada.contains("arm") || categoriaNormalizada.contains("brazo")) {
            return "Biceps brachii";
        }
        if (categoriaNormalizada.contains("shoulder") || categoriaNormalizada.contains("hombro")) {
            return "Deltoid";
        }
        if (categoriaNormalizada.contains("back") || categoriaNormalizada.contains("espalda")) {
            return "Latissimus dorsi";
        }
        return "Pectoralis major";
    }

    private static Map<String, Object> traduccion(Integer language, String name, String description, String licenseAuthor) {
        Map<String, Object> item = new java.util.LinkedHashMap<>();
        item.put("language", language);
        item.put("name", name);
        item.put("description_source", description);
        item.put("license_author", licenseAuthor);
        return item;
    }
}
