package pe.edu.upc.femfitai.services.implementations;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pe.edu.upc.femfitai.entities.Ejercicios;
import pe.edu.upc.femfitai.repositories.EjerciciosRepository;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WgerImagenSyncServiceTest {
    final WgerClient client = mock(WgerClient.class);
    final EjerciciosRepository repository = mock(EjerciciosRepository.class);
    final WgerSyncService service = new WgerSyncService(client, repository);
    final String url = "https://wger.de/media/exercise-images/81/remo.png";

    Map<String, Object> imagen(String url, boolean principal) {
        return new HashMap<>(Map.of("exercise", 81, "image", url, "is_main", principal,
                "license", 2, "license_author", "Autor imagen", "is_ai_generated", false, "style", "1"));
    }
    Map<String, Object> ejercicio(List<?> imagenes) {
        return Map.of("id", 81, "translations", List.of(Map.of("language", 4, "name", "Remo",
                "description_source", "Movimiento de prueba")), "images", imagenes);
    }
    Map<String, Object> pagina(Map<String, Object> ejercicio) {
        return Map.of("results", List.of(ejercicio));
    }
    void preparar(List<?> imagenes) {
        when(client.obtenerPaginaEjercicios(0)).thenReturn(pagina(ejercicio(imagenes)));
        when(client.obtenerLicencias()).thenReturn(Map.of("results", List.of(Map.of("id", 2,
                "short_name", "CC-BY-SA 4", "url", "http://creativecommons.org/licenses/by-sa/4.0/"))));
        when(repository.save(any(Ejercicios.class))).thenAnswer(i -> i.getArgument(0));
    }
    Ejercicios guardado() {
        var captor = ArgumentCaptor.forClass(Ejercicios.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }
    @Test void importaImagenPrincipalDelIdExactoConLaLicenciaDeLaImagen() {
        preparar(List.of(imagen(url+"?otra=1", false), imagen(url, true)));
        var result = service.sincronizarTodos();
        var item = guardado();
        assertEquals(url, item.getImagenUrl());
        assertEquals("Autor imagen", item.getImagenAutor());
        assertEquals("CC-BY-SA 4", item.getImagenLicencia());
        assertEquals("https://creativecommons.org/licenses/by-sa/4.0/", item.getImagenLicenciaUrl());
        assertEquals(1, result.imagenesImportadas()); assertTrue(result.completo());
    }
    @Test void admiteIlustracionesSinPrometerQueStylePhotoEsUnaFotografia() {
        var image = imagen(url, true); image.put("style", "4");
        preparar(List.of(image));
        service.sincronizarTodos();
        assertEquals(url, guardado().getImagenUrl());
    }
    @Test void imagenDeOtroEjercicioNuncaSeAsignaAunqueElNombreSeaIgual() {
        var image = imagen(url, true); image.put("exercise", 90);
        preparar(List.of(image));
        service.sincronizarTodos();
        assertNull(guardado().getImagenUrl());
    }
    @Test void conservaLasCuatroPropiedadesDeUnaImagenElegidaPorAdmin() {
        preparar(List.of(imagen(url, true)));
        var actual = new Ejercicios("Nombre antiguo", null, "Fuerza", "Anterior");
        actual.setIdEjercicio(15); actual.setWgerId(81);
        ImagenEjercicio.asignar(actual, "https://fotos.example.test/manual.jpg", "Grupo", "Licencia propia", "https://fotos.example.test/licencia");
        when(repository.findByWgerId(81)).thenReturn(Optional.of(actual));
        var result = service.sincronizarTodos();
        var item = guardado();
        assertEquals("Remo", item.getNombre()); assertEquals("Fuerza", item.getTipo());
        assertEquals("https://fotos.example.test/manual.jpg", item.getImagenUrl());
        assertEquals("Grupo", item.getImagenAutor()); assertEquals("Licencia propia", item.getImagenLicencia());
        assertEquals("https://fotos.example.test/licencia", item.getImagenLicenciaUrl());
        assertEquals(0, result.imagenesImportadas());
    }
    @Test void completaUnRegistroExistenteSinDuplicarloAunqueSusTextosNoCambien() {
        preparar(List.of(imagen(url, true)));
        var actual = new Ejercicios("Remo", null, "Fuerza", "Movimiento de prueba");
        actual.setIdEjercicio(15); actual.setWgerId(81);
        when(repository.findByWgerId(81)).thenReturn(Optional.of(actual));
        var result = service.sincronizarTodos();
        assertSame(actual, guardado()); assertEquals(15, actual.getIdEjercicio());
        assertEquals(url, actual.getImagenUrl());
        assertEquals(0, result.insertados()); assertEquals(1, result.actualizados());
        assertEquals(1, result.imagenesImportadas());
    }
    @Test void repetirSincronizacionNoInsertaDuplicadosNiVuelveAContarLaImagen() {
        preparar(List.of(imagen(url, true)));
        var actual = new Ejercicios("Remo", null, "Fuerza", "Movimiento de prueba");
        actual.setWgerId(81); ImagenEjercicio.asignar(actual, url, "Autor", "CC0", "https://creativecommons.org/publicdomain/zero/1.0/");
        when(repository.findByWgerId(81)).thenReturn(Optional.of(actual));
        var result = service.sincronizarTodos();
        assertEquals(0, result.imagenesImportadas()); assertEquals(0, result.insertados());
        verify(repository, never()).save(any());
    }
    @Test void sinImagenesNoInventaUnaFotoNiConsultaLicenciasInnecesariamente() {
        preparar(List.of());
        var result = service.sincronizarTodos();
        assertNull(guardado().getImagenUrl()); assertEquals(0, result.imagenesImportadas());
        verify(client, never()).obtenerLicencias();
    }
    @Test void descartaUrlsInsegurasYContinuaConUnaAlternativaValida() {
        for (String unsafe : List.of("javascript:alert(1)", "http://wger.de/media/exercise-images/81/a.png",
                "https://otra.example.test/foto.jpg", "https://user:pass@wger.de/media/exercise-images/81/a.png",
                "https://wger.de/media/exercise-images/81/a.png#fragmento")) {
            preparar(List.of(imagen(unsafe, true), imagen(url, false)));
            service.sincronizarTodos();
        }
        var captor = ArgumentCaptor.forClass(Ejercicios.class);
        verify(repository, times(5)).save(captor.capture());
        assertTrue(captor.getAllValues().stream().allMatch(e -> url.equals(e.getImagenUrl())));
    }
    @Test void licenciaDesconocidaOImagenDeIaNoSeImportan() {
        var unknown = imagen(url, true); unknown.put("license", 999);
        var artificial = imagen(url, true); artificial.put("is_ai_generated", true);
        preparar(List.of(unknown, artificial));
        service.sincronizarTodos();
        assertNull(guardado().getImagenUrl());
    }
    @Test void unaCaidaDeLicenciasConservaLaImportacionDeTextoEInformaResultadoParcial() {
        preparar(List.of(imagen(url, true)));
        when(client.obtenerLicencias()).thenThrow(new IllegalStateException("API de prueba no disponible"));
        var result = service.sincronizarTodos();
        assertEquals("Remo", guardado().getNombre());
        assertEquals(1, result.insertados()); assertFalse(result.completo());
        assertTrue(result.mensaje().contains("licencias")); assertEquals(0, result.imagenesImportadas());
    }
    @Test void consultaLicenciasUnaSolaVezParaTodasLasPaginas() {
        preparar(List.of(imagen(url, true)));
        when(client.obtenerPaginaEjercicios(0)).thenReturn(Map.of("results", List.of(ejercicio(List.of(imagen(url,true)))), "next", "https://wger.de/api/v2/exerciseinfo/?offset=20"));
        var otro = new HashMap<>(ejercicio(List.of(imagen(url,true)))); otro.put("id",82);
        when(client.obtenerPaginaEjercicios(20)).thenReturn(pagina(otro));
        service.sincronizarTodos();
        verify(client, times(1)).obtenerLicencias();
        verify(client).obtenerPaginaEjercicios(20);
    }
    @Test void creditosDemasiadoLargosNoRompenLaImportacionDelEjercicio() {
        var image = imagen(url, true); image.put("license_author", "x".repeat(256));
        preparar(List.of(image));
        service.sincronizarTodos();
        assertNull(guardado().getImagenUrl());
    }
}
