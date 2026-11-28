package pe.edu.upc.femfitai.controllers;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upc.femfitai.entities.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/administracion")
public class AdministracionCiclosController {

    private final EntityManager em;

    public AdministracionCiclosController(EntityManager em) {
        this.em = em;
    }

    private record Modulo(
            String entidad,
            String idCampo,
            String rutaUsuario
    ) {}

    private record RegistroAdminDTO(
            Integer idUsuario,
            Map<String, Object> datos,
            boolean modificable
    ) {}

    private static final Map<String, Modulo> MODULOS = Map.ofEntries(

            Map.entry("ciclos",
                    new Modulo("Ciclos", "idCiclo", "usuario.idUsuario")),

            Map.entry("detalle-diario",
                    new Modulo("DetalleDiarioCiclo", "idDetalleDiarioCiclo",
                            "ciclo.usuario.idUsuario")),

            Map.entry("rutinas",
                    new Modulo("Rutinas", "idRutina", "usuario.idUsuario")),

            Map.entry("rutina-ejercicios",
                    new Modulo("RutinaEjercicios", "idRutinaEjercicio",
                            "rutina.usuario.idUsuario")),

            Map.entry("sesiones",
                    new Modulo("SesionesEntrenamiento", "idSesion",
                            "usuario.idUsuario")),

            Map.entry("detalle-sesion",
                    new Modulo("DetalleSesion", "idDetalle",
                            "sesion.usuario.idUsuario")),

            Map.entry("detalle-serie",
                    new Modulo("DetalleSerie", "idSerie",
                            "detalle.sesion.usuario.idUsuario")),

            Map.entry("progreso",
                    new Modulo("Progreso", "idProgreso", "usuario.idUsuario")),

            Map.entry("perfiles",
                    new Modulo("PerfilEntrenamiento", "idPerfil",
                            "usuario.idUsuario")),

            Map.entry("recomendaciones",
                    new Modulo("RecomendacionesIA", "idRecomendacion",
                            "usuario.idUsuario"))
    );

    // LISTAR REGISTROS DE UN MÓDULO
    @GetMapping("/{modulo}")
    @Transactional(readOnly = true)
    public Page<RegistroAdminDTO> listar(
            @PathVariable String modulo,
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        Modulo config = obtenerModulo(modulo);

        if (pagina < 0 || tamano < 1 || tamano > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los parámetros de paginación no son válidos"
            );
        }

        Pageable pageable = PageRequest.of(pagina, tamano);

        String condicion = idUsuario == null
                ? ""
                : " WHERE e." + config.rutaUsuario() + " = :idUsuario";

        String jpql = "SELECT e FROM " + config.entidad() + " e"
                + condicion
                + " ORDER BY e." + config.idCampo() + " DESC";

        TypedQuery<Object> query =
                em.createQuery(jpql, Object.class);

        if (idUsuario != null) {
            query.setParameter("idUsuario", idUsuario);
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<RegistroAdminDTO> registros = query.getResultList()
                .stream()
                .map(e -> convertir(modulo, e))
                .toList();

        String countJpql = "SELECT COUNT(e) FROM "
                + config.entidad() + " e" + condicion;

        TypedQuery<Long> countQuery =
                em.createQuery(countJpql, Long.class);

        if (idUsuario != null) {
            countQuery.setParameter("idUsuario", idUsuario);
        }

        long total = countQuery.getSingleResult();

        return new PageImpl<>(registros, pageable, total);
    }

    // CONSULTAR UN REGISTRO POR SU ID
    @GetMapping("/{modulo}/{id}")
    @Transactional(readOnly = true)
    public RegistroAdminDTO buscar(
            @PathVariable String modulo,
            @PathVariable Long id) {

        Modulo config = obtenerModulo(modulo);

        String jpql = "SELECT e FROM " + config.entidad()
                + " e WHERE e." + config.idCampo() + " = :id";

        TypedQuery<Object> query =
                em.createQuery(jpql, Object.class);

        Object idBuscado = modulo.equals("ciclos")
                ? id
                : id.intValue();

        query.setParameter("id", idBuscado);

        try {
            Object entidad = query.getSingleResult();
            return convertir(modulo, entidad);

        } catch (NoResultException ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se encontró el registro solicitado"
            );
        }
    }

    // VALIDAR QUE EL MÓDULO EXISTA
    private Modulo obtenerModulo(String modulo) {
        Modulo config = MODULOS.get(modulo);

        if (config == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Módulo administrativo no encontrado: " + modulo
            );
        }

        return config;
    }

    // CONVERTIR LAS ENTIDADES AL FORMATO QUE ESPERA ANGULAR
    private RegistroAdminDTO convertir(String modulo, Object entidad) {

        Map<String, Object> datos = new LinkedHashMap<>();
        Integer idUsuario;

        boolean modificable = true;

        switch (modulo) {

            case "ciclos" -> {
                Ciclos c = (Ciclos) entidad;

                idUsuario = c.getUsuario().getIdUsuario();

                datos.put("idCiclo", c.getIdCiclo());
                datos.put("idUsuario", idUsuario);
                datos.put("fechaInicio", c.getFechaInicio());
                datos.put("fechaFinEstimada", c.getFechaFinEstimada());
                datos.put("fechaReal", c.getFechaReal());
            }

            case "detalle-diario" -> {
                DetalleDiarioCiclo d = (DetalleDiarioCiclo) entidad;

                idUsuario = d.getCiclo().getUsuario().getIdUsuario();

                datos.put("idDetalleDiarioCiclo",
                        d.getIdDetalleDiarioCiclo());
                datos.put("idCiclo", d.getCiclo().getIdCiclo());
                datos.put("fecha", d.getFecha());
                datos.put("faseRegistrada", d.getFaseRegistrada());
                datos.put("nivelEnergia", d.getNivelEnergia());
                datos.put("observaciones", d.getObservaciones());
            }

            case "rutinas" -> {
                Rutinas r = (Rutinas) entidad;

                idUsuario = r.getUsuario().getIdUsuario();

                datos.put("idRutina", r.getIdRutina());
                datos.put("idUsuario", idUsuario);
                datos.put("nombre", r.getNombre());
                datos.put("objetivo", r.getObjetivo());
                datos.put("nivel", r.getNivel());
                datos.put("fechaCreacion", r.getFechaCreacion());
                datos.put("estado", r.getEstado());
            }

            case "rutina-ejercicios" -> {
                RutinaEjercicios re = (RutinaEjercicios) entidad;

                idUsuario = re.getRutina().getUsuario().getIdUsuario();

                datos.put("idRutinaEjercicio",
                        re.getIdRutinaEjercicio());
                datos.put("idRutina", re.getRutina().getIdRutina());
                datos.put("idEjercicio", re.getEjercicio().getIdEjercicio());
                datos.put("series", re.getSeries());
                datos.put("repeticiones", re.getRepeticiones());
                datos.put("descansoSeg", re.getDescansoSeg());
            }

            case "sesiones" -> {
                SesionesEntrenamiento s =
                        (SesionesEntrenamiento) entidad;

                idUsuario = s.getUsuario().getIdUsuario();

                datos.put("idSesion", s.getIdSesion());
                datos.put("idRutina", s.getRutina().getIdRutina());
                datos.put("fecha", s.getFecha());
                datos.put("estado", s.getEstado());
                datos.put("duracionMin", s.getDuracionMin());
                datos.put("nivelEnergia", s.getNivelEnergia());
                datos.put("esfuerzoPercibido", s.getEsfuerzoPercibido());

                modificable = "EN_CURSO".equalsIgnoreCase(s.getEstado());
            }

            case "detalle-sesion" -> {
                DetalleSesion d = (DetalleSesion) entidad;

                idUsuario = d.getSesion().getUsuario().getIdUsuario();

                datos.put("idDetalle", d.getIdDetalle());
                datos.put("idSesion", d.getSesion().getIdSesion());
                datos.put("idEjercicio", d.getEjercicio().getIdEjercicio());
                datos.put("observacion", d.getObservacion());

                modificable = "EN_CURSO".equalsIgnoreCase(
                        d.getSesion().getEstado());
            }

            case "detalle-serie" -> {
                DetalleSerie d = (DetalleSerie) entidad;

                idUsuario = d.getDetalle().getSesion()
                        .getUsuario().getIdUsuario();

                datos.put("idSerie", d.getIdSerie());
                datos.put("idDetalle", d.getDetalle().getIdDetalle());
                datos.put("numeroSerie", d.getNumeroSerie());
                datos.put("repeticiones", d.getRepeticiones());
                datos.put("pesoKg", d.getPesoKg());

                modificable = "EN_CURSO".equalsIgnoreCase(
                        d.getDetalle().getSesion().getEstado());
            }

            case "progreso" -> {
                Progreso p = (Progreso) entidad;

                idUsuario = p.getUsuario().getIdUsuario();

                datos.put("idProgreso", p.getIdProgreso());
                datos.put("idUsuario", idUsuario);
                datos.put("fecha", p.getFecha());
                datos.put("pesoKg", p.getPesoKg());
                datos.put("medidaOpcional", p.getMedidaOpcional());
                datos.put("notaPersonal", p.getNotaPersonal());
            }

            case "perfiles" -> {
                PerfilEntrenamiento p = (PerfilEntrenamiento) entidad;

                idUsuario = p.getUsuario().getIdUsuario();

                datos.put("idPerfil", p.getIdPerfil());
                datos.put("idUsuario", idUsuario);
                datos.put("nivelEntrenamiento", p.getNivelEntrenamiento());
                datos.put("objetivoPrincipal", p.getObjetivoPrincipal());
                datos.put("diasDisponibles", p.getDiasDisponibles());
                datos.put("tiempoDisponible", p.getTiempoDisponible());
                datos.put("fechaNacimiento", p.getFechaNacimiento());
            }

            case "recomendaciones" -> {
                RecomendacionesIA r = (RecomendacionesIA) entidad;

                idUsuario = r.getUsuario().getIdUsuario();

                datos.put("idRecomendacion", r.getIdRecomendacion());
                datos.put("idUsuario", idUsuario);
                datos.put("idRutina", r.getRutina() == null
                        ? null : r.getRutina().getIdRutina());
                datos.put("fecha", r.getFecha());
                datos.put("tipo", r.getTipo());
                datos.put("contenido", r.getContenido());
                datos.put("motivo", r.getMotivo());
                datos.put("aceptada", r.getAceptada());
            }

            default -> throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Módulo administrativo no soportado"
            );
        }

        return new RegistroAdminDTO(
                idUsuario,
                datos,
                modificable
        );
    }
}