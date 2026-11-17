package pe.edu.upc.femfitai.services.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import pe.edu.upc.femfitai.entities.Ejercicios;
import pe.edu.upc.femfitai.repositories.EjerciciosRepository;
import pe.edu.upc.femfitai.services.interfaces.IWgerSyncService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class WgerSyncService implements IWgerSyncService {
    private static final Logger log = LoggerFactory.getLogger(WgerSyncService.class);
    private static final Pattern CONTENIDO_PELIGROSO = Pattern.compile(
            "(?i)<\\s*(script|iframe|object|embed|svg)\\b|javascript\\s*:|\\bon[a-z]+\\s*=");

    private final WgerClient wgerClient;
    private final EjerciciosRepository ejerciciosRepository;

    public WgerSyncService(WgerClient wgerClient, EjerciciosRepository ejerciciosRepository) {
        this.wgerClient = wgerClient;
        this.ejerciciosRepository = ejerciciosRepository;
    }

    @Override
    public ResultadoSincronizacion sincronizarTodos() {
        int totalConsultados = 0;
        int insertados = 0;
        int actualizados = 0;
        int omitidos = 0;
        int offset = 0;
        boolean completo = true;
        Integer offsetError = null;
        String mensaje = null;

        while (true) {
            Map<String, Object> pagina;
            try {
                pagina = wgerClient.obtenerPaginaEjercicios(offset);
            } catch (RuntimeException ex) {
                completo = false;
                offsetError = offset;
                mensaje = "Sincronización incompleta: error consultando la API externa de wger en offset " + offset;
                log.warn(mensaje, ex);
                break;
            }

            List<?> resultados = asList(pagina.get("results"));
            if (resultados == null || resultados.isEmpty()) {
                break;
            }

            for (Object item : resultados) {
                if (!(item instanceof Map<?, ?> ejercicioMap)) {
                    omitidos++;
                    continue;
                }

                totalConsultados++;
                Integer wgerId = toInteger(ejercicioMap.get("id"));
                if (wgerId == null) {
                    omitidos++;
                    continue;
                }

                Ejercicios existente = ejerciciosRepository.findByWgerId(wgerId).orElse(null);
                if (existente != null && existente.getWgerId() == null) {
                    omitidos++;
                    continue;
                }

                Ejercicios ejercicio = mapearEjercicio(ejercicioMap, wgerId);
                if (ejercicio == null) {
                    omitidos++;
                    continue;
                }

                try {
                    if (existente == null) {
                        ejerciciosRepository.save(ejercicio);
                        insertados++;
                    } else if (actualizarEjercicioExistente(existente, ejercicio)) {
                        ejerciciosRepository.save(existente);
                        actualizados++;
                    } else {
                        omitidos++;
                    }
                } catch (DataIntegrityViolationException ex) {
                    log.warn("Se omitió un ejercicio duplicado o inválido de wger con ID {}", wgerId, ex);
                    omitidos++;
                }
            }

            Object next = pagina.get("next");
            if (next == null || next.toString().isBlank()) {
                break;
            }

            offset += 20;
        }

        return new ResultadoSincronizacion(totalConsultados, insertados, actualizados, omitidos, completo, offsetError, mensaje);
    }

    private Ejercicios mapearEjercicio(Map<?, ?> ejercicioMap, Integer wgerId) {
        String nombre = seleccionarNombre(ejercicioMap);
        if (nombre == null || nombre.isBlank()) {
            return null;
        }

        String descripcion = descripcionSegura(construirDescripcion(ejercicioMap));
        String grupoMuscular = resolverGrupoMuscular(ejercicioMap);

        Ejercicios ejercicio = new Ejercicios(nombre, grupoMuscular, null, descripcion);
        ejercicio.setWgerId(wgerId);
        return ejercicio;
    }

    private String seleccionarNombre(Map<?, ?> ejercicioMap) {
        List<Map<?, ?>> traducciones = obtenerListaMapas(ejercicioMap.get("translations"));
        if (traducciones == null || traducciones.isEmpty()) {
            return null;
        }

        for (Map<?, ?> traduccion : traducciones) {
            if (esEspanol(traduccion) && hasName(traduccion)) {
                return limpiarTexto(traduccion.get("name"));
            }
        }

        for (Map<?, ?> traduccion : traducciones) {
            if (esIngles(traduccion) && hasName(traduccion)) {
                return limpiarTexto(traduccion.get("name"));
            }
        }

        for (Map<?, ?> traduccion : traducciones) {
            if (hasName(traduccion)) {
                return limpiarTexto(traduccion.get("name"));
            }
        }

        return null;
    }

    private String construirDescripcion(Map<?, ?> ejercicioMap) {
        List<Map<?, ?>> traducciones = obtenerListaMapas(ejercicioMap.get("translations"));
        if (traducciones == null || traducciones.isEmpty()) {
            return null;
        }

        Map<?, ?> seleccionada = null;
        for (Map<?, ?> traduccion : traducciones) {
            if (esEspanol(traduccion) && hasName(traduccion)) {
                seleccionada = traduccion;
                break;
            }
        }
        if (seleccionada == null) {
            for (Map<?, ?> traduccion : traducciones) {
                if (esIngles(traduccion) && hasName(traduccion)) {
                    seleccionada = traduccion;
                    break;
                }
            }
        }
        if (seleccionada == null) {
            for (Map<?, ?> traduccion : traducciones) {
                if (hasName(traduccion)) {
                    seleccionada = traduccion;
                    break;
                }
            }
        }

        String descripcion = seleccionada == null ? null : limpiarTexto(seleccionada.get("description_source"));
        String autorPrincipal = limpiarTexto(ejercicioMap.get("license_author"));
        String autorTraduccion = seleccionada == null ? null : limpiarTexto(seleccionada.get("license_author"));
        String licenciaNombre = limpiarTexto(obtenerValor(ejercicioMap.get("license"), "full_name"));
        if (licenciaNombre == null || licenciaNombre.isBlank()) {
            licenciaNombre = limpiarTexto(obtenerValor(ejercicioMap.get("license"), "name"));
        }
        String licenciaUrl = limpiarTexto(obtenerValor(ejercicioMap.get("license"), "url"));
        String autor = combinarSinDuplicados(autorPrincipal, autorTraduccion);

        StringBuilder texto = new StringBuilder();
        if (descripcion != null && !descripcion.isBlank()) {
            texto.append(descripcion);
        }

        if (autor != null && !autor.isBlank()) {
            if (!texto.isEmpty()) {
                texto.append("\n");
            }
            texto.append("Autor: ").append(autor);
        }

        if (licenciaNombre != null && !licenciaNombre.isBlank()) {
            if (!texto.isEmpty()) {
                texto.append("\n");
            }
            texto.append("Licencia: ").append(licenciaNombre);
        }

        if (licenciaUrl != null && !licenciaUrl.isBlank()) {
            if (!texto.isEmpty()) {
                texto.append("\n");
            }
            texto.append("URL licencia: ").append(licenciaUrl);
        }

        String valor = texto.toString();
        return valor.isBlank() ? null : valor;
    }

    private String resolverGrupoMuscular(Map<?, ?> ejercicioMap) {
        List<Map<?, ?>> principales = obtenerListaMapas(ejercicioMap.get("muscles"));
        if (principales != null) {
            for (Map<?, ?> músculo : principales) {
                String nombre = traduccionNombreMusculo(músculo);
                if (nombre != null && !nombre.isBlank()) {
                    return nombre;
                }
            }
        }

        Object categoria = ejercicioMap.get("category");
        if (categoria instanceof Map<?, ?> categoriaMap) {
            String nombreCategoria = traduccionNombreMusculo(categoriaMap);
            if (nombreCategoria != null && !nombreCategoria.isBlank()) {
                return nombreCategoria;
            }
        }

        return null;
    }

    private String traduccionNombreMusculo(Map<?, ?> mapa) {
        if (mapa == null) {
            return null;
        }

        String nombre = limpiarTexto(obtenerValor(mapa, "name"));
        if (nombre == null || nombre.isBlank()) {
            nombre = limpiarTexto(obtenerValor(mapa, "name_en"));
        }
        if (nombre == null || nombre.isBlank()) {
            nombre = limpiarTexto(obtenerValor(mapa, "nameSpanish"));
        }
        if (nombre == null || nombre.isBlank()) {
            return null;
        }

        String normalizado = nombre.toLowerCase(Locale.ROOT);

        if (normalizado.contains("arms")) {
            return "Brazos";
        }
        if (normalizado.contains("abs") || normalizado.contains("abdominis") || normalizado.contains("abdominal")) {
            return "Abdominales";
        }
        if (normalizado.contains("legs") || normalizado.contains("leg")) {
            return "Piernas";
        }
        if (normalizado.contains("chest") || normalizado.contains("pecho")) {
            return "Pecho";
        }
        if (normalizado.contains("back") || normalizado.contains("espalda") || normalizado.contains("lat") || normalizado.contains("spina")) {
            return "Espalda";
        }
        if (normalizado.contains("shoulders") || normalizado.contains("shoulder") || normalizado.contains("hombro")) {
            return "Hombros";
        }

        if (normalizado.contains("biceps femoris") || normalizado.contains("hamstrings") || normalizado.contains("hamstring")) {
            return "Isquiotibiales";
        }
        if (normalizado.contains("biceps brachii") || normalizado.contains("biceps")) {
            return "Bíceps";
        }
        if (normalizado.contains("triceps")) {
            return "Tríceps";
        }
        if (normalizado.contains("quadriceps femoris") || normalizado.contains("quads") || normalizado.contains("quadriceps")) {
            return "Cuádriceps";
        }
        if (normalizado.contains("gluteus maximus") || normalizado.contains("glutes") || normalizado.contains("glute")) {
            return "Glúteos";
        }
        if (normalizado.contains("rectus abdominis") || normalizado.contains("abs") || normalizado.contains("abdominis")) {
            return "Abdominales";
        }
        if (normalizado.contains("gastrocnemius") || normalizado.contains("calves") || normalizado.contains("calf")) {
            return "Pantorrillas";
        }
        if (normalizado.contains("trapezius") || normalizado.contains("trap")) {
            return "Trapecios";
        }
        if (normalizado.contains("adductor")) {
            return "Aductores";
        }
        if (normalizado.contains("abductor")) {
            return "Abductores";
        }
        if (normalizado.contains("pectoral") || normalizado.contains("pecho")) {
            return "Pecho";
        }
        if (normalizado.contains("deltoid") || normalizado.contains("shoulder") || normalizado.contains("hombro")) {
            return "Hombros";
        }
        if (normalizado.contains("abdom") || normalizado.contains("core") || normalizado.contains("abdomen")) {
            return "Abdominales";
        }
        if (normalizado.contains("pierna") || normalizado.contains("leg") || normalizado.contains("cuadriceps")) {
            return "Piernas";
        }
        if (normalizado.contains("neck") || normalizado.contains("cuello")) {
            return "Cuello";
        }
        return nombre;
    }

    private static String limpiarTexto(Object valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.toString().trim();
        return texto.isBlank() ? null : texto;
    }

    private static boolean hasName(Map<?, ?> traduccion) {
        return limpiarTexto(traduccion.get("name")) != null;
    }

    private static boolean esEspanol(Map<?, ?> traduccion) {
        Object idioma = traduccion.get("language");
        return idioma != null && idioma.toString().equals("4");
    }

    private static boolean esIngles(Map<?, ?> traduccion) {
        Object idioma = traduccion.get("language");
        return idioma != null && idioma.toString().equals("2");
    }

    private static Object obtenerValor(Object origen, String clave) {
        if (!(origen instanceof Map<?, ?> mapa)) {
            return null;
        }
        return mapa.get(clave);
    }

    private static Integer toInteger(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof Number numero) {
            return numero.intValue();
        }
        try {
            return Integer.valueOf(valor.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<?, ?>> obtenerListaMapas(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof List<?> lista) {
            List<Map<?, ?>> resultado = new ArrayList<>();
            for (Object item : lista) {
                if (item instanceof Map<?, ?> mapa) {
                    resultado.add((Map<?, ?>) mapa);
                }
            }
            return resultado;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<?> asList(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof List<?> lista) {
            return lista;
        }
        return List.of();
    }

    private String descripcionSegura(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String resultado = valor;
        resultado = resultado.replace("&aacute;", "á")
                .replace("&eacute;", "é")
                .replace("&iacute;", "í")
                .replace("&oacute;", "ó")
                .replace("&uacute;", "ú")
                .replace("&Aacute;", "Á")
                .replace("&Eacute;", "É")
                .replace("&Iacute;", "Í")
                .replace("&Oacute;", "Ó")
                .replace("&Uacute;", "Ú")
                .replace("&ntilde;", "ñ")
                .replace("&Ntilde;", "Ñ")
                .replace("&uuml;", "ü")
                .replace("&Uuml;", "Ü")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&nbsp;", " ")
                .replace("&iexcl;", "¡")
                .replace("&iquest;", "¿")
                .replace("&egrave;", "è")
                .replace("&igrave;", "ì")
                .replace("&ograve;", "ò")
                .replace("&ugrave;", "ù")
                .replace("&Egrave;", "È")
                .replace("&Igrave;", "Ì")
                .replace("&Ograve;", "Ò")
                .replace("&Ugrave;", "Ù")
                .replace("&ccedil;", "ç")
                .replace("&Ccedil;", "Ç");
        return resultado;
    }

    private boolean actualizarEjercicioExistente(Ejercicios existente, Ejercicios ejercicio) {
        if (Objects.equals(existente.getNombre(), ejercicio.getNombre())
                && Objects.equals(existente.getGrupoMuscular(), ejercicio.getGrupoMuscular())
                && Objects.equals(existente.getDescripcion(), ejercicio.getDescripcion())) {
            return false;
        }

        existente.setNombre(ejercicio.getNombre());
        existente.setGrupoMuscular(ejercicio.getGrupoMuscular());
        existente.setDescripcion(ejercicio.getDescripcion());
        return true;
    }

    private static String combinarSinDuplicados(String... valores) {
        Set<String> distintos = new LinkedHashSet<>();
        for (String valor : valores) {
            if (valor == null) {
                continue;
            }
            String normalizado = valor.trim();
            if (!normalizado.isBlank()) {
                distintos.add(normalizado);
            }
        }
        if (distintos.isEmpty()) {
            return null;
        }
        return String.join(", ", distintos);
    }
}