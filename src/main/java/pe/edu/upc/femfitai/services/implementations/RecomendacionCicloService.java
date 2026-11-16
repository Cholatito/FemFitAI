package pe.edu.upc.femfitai.services.implementations;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.femfitai.dtos.GenerarRecomendacionCicloDTO;
import pe.edu.upc.femfitai.dtos.RecomendacionesIADTO;
import pe.edu.upc.femfitai.entities.*;
import pe.edu.upc.femfitai.repositories.CiclosRepository;
import pe.edu.upc.femfitai.repositories.DetalleDiarioCicloRepository;
import pe.edu.upc.femfitai.repositories.PerfilEntrenamientoRepository;
import pe.edu.upc.femfitai.repositories.RecomendacionesIARepository;

import java.time.LocalDateTime;
import java.util.Set;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class RecomendacionCicloService {

    private final UsuarioActualService actual;
    private final PerfilEntrenamientoRepository perfiles;
    private final CiclosRepository ciclos;
    private final DetalleDiarioCicloRepository detalles;
    private final RecomendacionesIARepository recomendaciones;
    private final GeminiService gemini;

    public RecomendacionCicloService(
            UsuarioActualService actual,
            PerfilEntrenamientoRepository perfiles,
            CiclosRepository ciclos,
            DetalleDiarioCicloRepository detalles,
            RecomendacionesIARepository recomendaciones,
            GeminiService gemini) {

        this.actual = actual;
        this.perfiles = perfiles;
        this.ciclos = ciclos;
        this.detalles = detalles;
        this.recomendaciones = recomendaciones;
        this.gemini = gemini;
    }

    @Transactional
    public RecomendacionesIADTO generar(
            GenerarRecomendacionCicloDTO request) {

        // 1. Validar solicitud
        if (request == null || request.tipo() == null
                || !request.tipo().trim().equalsIgnoreCase("ENTRENAMIENTO")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tipo de recomendación debe ser ENTRENAMIENTO");
        }

        // 2. Obtener usuaria autenticada mediante JWT
        Usuarios usuario = actual.obtener();
        Integer idUsuario = usuario.getIdUsuario();

        // 3. Obtener perfil de entrenamiento
        PerfilEntrenamiento perfil = perfiles
                .findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Completa tu perfil de entrenamiento antes de solicitar recomendaciones"));

        // Validar datos del perfil
        if (perfil.getNivelEntrenamiento() == null
                || perfil.getNivelEntrenamiento().isBlank()
                || perfil.getObjetivoPrincipal() == null
                || perfil.getObjetivoPrincipal().isBlank()
                || perfil.getDiasDisponibles() == null
                || perfil.getDiasDisponibles() < 1
                || perfil.getDiasDisponibles() > 7
                || perfil.getTiempoDisponible() == null
                || perfil.getTiempoDisponible() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Completa todos los datos de tu perfil de entrenamiento");
        }

        // 4. Obtener ciclo menstrual activo
        Ciclos ciclo = ciclos
                .findFirstByUsuario_IdUsuarioAndFechaRealIsNullOrderByFechaInicioDesc(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Necesitas registrar un ciclo menstrual activo"));


// 5. Estimar la fase menstrual desde la fecha de inicio
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Lima"));

        long diaCiclo = ChronoUnit.DAYS.between(
                ciclo.getFechaInicio(), hoy
        ) + 1;

        if (diaCiclo < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de inicio del ciclo no es válida");
        }

// Duración estimada del ciclo
        long duracionCiclo = ciclo.getFechaFinEstimada() != null
                ? ChronoUnit.DAYS.between(
                ciclo.getFechaInicio(),
                ciclo.getFechaFinEstimada())
                : 28;

// Usar 28 días si la duración estimada no está dentro
// del intervalo habitual considerado por este algoritmo.
        if (duracionCiclo < 21 || duracionCiclo > 35) {
            duracionCiclo = 28;
        }

// No estimar una fase si ya pasó la duración prevista.
        if (diaCiclo > duracionCiclo) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ciclo superó su duración estimada. Actualiza el registro del ciclo.");
        }

        String faseEstimada = estimarFase(
                (int) diaCiclo,
                (int) duracionCiclo
        );

// 6. Consultar detalles diarios opcionales
        List<DetalleDiarioCiclo> registros =
                detalles.findByCiclo_IdCicloOrderByFechaAsc(
                        ciclo.getIdCiclo()
                );

// Si existe una fase registrada hoy, tiene prioridad.
        Optional<DetalleDiarioCiclo> registroHoy =
                registros.stream()
                        .filter(d -> hoy.equals(d.getFecha()))
                        .filter(d -> d.getFaseRegistrada() != null
                                && !d.getFaseRegistrada().isBlank())
                        .max(Comparator.comparing(
                                DetalleDiarioCiclo::getIdDetalleDiarioCiclo
                        ));

// Si no existe una fase registrada hoy, usar la estimación.
        String fase = registroHoy
                .map(DetalleDiarioCiclo::getFaseRegistrada)
                .orElse(faseEstimada);

        String origenFase = registroHoy.isPresent()
                ? "Registrada por la usuaria hoy"
                : "Estimada según la fecha de inicio del ciclo";

// Buscar energía registrada durante los últimos 7 días.
        Optional<DetalleDiarioCiclo> registroEnergia =
                registros.stream()
                        .filter(d -> d.getFecha() != null)
                        .filter(d -> !d.getFecha().isAfter(hoy))
                        .filter(d -> !d.getFecha().isBefore(
                                hoy.minusDays(7)))
                        .filter(d -> d.getNivelEnergia() != null)
                        .max(Comparator.comparing(
                                DetalleDiarioCiclo::getFecha
                        ));

        Integer energia = registroEnergia
                .map(DetalleDiarioCiclo::getNivelEnergia)
                .orElse(null);

        String energiaTexto = energia != null
                ? energia + " de 5"
                : "No registrada";

        String fechaEnergia = registroEnergia
                .map(d -> d.getFecha().toString())
                .orElse("No disponible");


        // 6. Construir contexto para Gemini

        String prompt = """
        Eres FemFitAI, una asistente de entrenamiento y
        bienestar femenino.

        Genera una recomendación personalizada de entrenamiento.

        DATOS DE LA USUARIA:

        Fase menstrual: %s
        Origen de la fase: %s
        Día del ciclo: %d de %d
        Nivel: %s
        Objetivo: %s
        Días disponibles por semana: %d
        Minutos por sesión: %d
        Energía reciente: %s
        Fecha del registro de energía: %s

        INSTRUCCIONES:

        1. Responde en español, de forma amigable y clara.
        2. Adapta el entrenamiento al nivel y objetivo.
        3. Considera la fase menstrual como una estimación
           si fue calculada automáticamente.
        4. No asumas que todas las mujeres experimentan
           los mismos efectos en cada fase.
        5. Si no hay energía registrada, no inventes ese dato.
           Ofrece opciones flexibles de intensidad.
        6. Incluye ejercicios, series, repeticiones y descanso
           cuando corresponda.
        7. Respeta los días y minutos disponibles.
        8. No diagnostiques enfermedades ni recomiendes
           medicamentos.
        9. No generes dietas ni planes alimenticios.
        10. Si existen síntomas preocupantes, recomienda
            consultar con un profesional de salud.

        FORMATO:

        Resumen:
        Propuesta semanal:
        Adaptación a la fase menstrual:
        Advertencia:
        """.formatted(
                fase,
                origenFase,
                diaCiclo,
                duracionCiclo,
                perfil.getNivelEntrenamiento(),
                perfil.getObjetivoPrincipal(),
                perfil.getDiasDisponibles(),
                perfil.getTiempoDisponible(),
                energiaTexto,
                fechaEnergia
        );


        // 7. Solicitar generación a Gemini
        String contenido;

        try {
            contenido = gemini.responder(prompt);

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No fue posible generar la recomendación. Inténtalo nuevamente.",
                    e
            );
        }

        if (contenido == null || contenido.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Gemini no devolvió una recomendación válida");
        }

        // 8. Registrar el motivo de la recomendación
        String motivo = String.format(
                "Recomendación basada en la fase %s (%s), día %d del ciclo y energía: %s.",
                fase,
                origenFase,
                diaCiclo,
                energiaTexto
        );

        // 9. Guardar recomendación en PostgreSQL
        RecomendacionesIA nueva = new RecomendacionesIA(
                usuario,
                null,
                LocalDateTime.now(),
                "ENTRENAMIENTO",
                contenido,
                motivo,
                false
        );

        RecomendacionesIA guardada =
                recomendaciones.saveAndFlush(nueva);

        // 10. Devolver DTO
        return new RecomendacionesIADTO(
                guardada.getIdRecomendacion(),
                idUsuario,
                null,
                guardada.getFecha(),
                guardada.getTipo(),
                guardada.getContenido(),
                guardada.getMotivo(),
                guardada.getAceptada()
        );
    }


    private String estimarFase(int diaCiclo, int duracionCiclo) {

        // Menstruación: primeros cinco días.
        if (diaCiclo <= 5) {
            return "Menstrual";
        }

        // Estimación de ovulación: aproximadamente
        // 14 días antes del siguiente ciclo.
        int diaOvulacion = duracionCiclo - 14;

        // Ventana aproximada de ovulación.
        int inicioOvulacion = Math.max(6, diaOvulacion - 1);
        int finOvulacion = diaOvulacion + 1;

        if (diaCiclo < inicioOvulacion) {
            return "Folicular";
        }

        if (diaCiclo <= finOvulacion) {
            return "Ovulatoria";
        }

        return "Lútea";
    }

}