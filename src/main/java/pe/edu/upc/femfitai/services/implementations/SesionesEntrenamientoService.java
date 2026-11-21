package pe.edu.upc.femfitai.services.implementations;

import pe.edu.upc.femfitai.entities.Rutinas;
import pe.edu.upc.femfitai.entities.Usuarios;
import pe.edu.upc.femfitai.services.interfaces.ISesionesEntrenamientoService;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.femfitai.dtos.FinalizarSesionDTO;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTO;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTODetalle;
import pe.edu.upc.femfitai.entities.DetalleSesion;
import pe.edu.upc.femfitai.entities.RutinaEjercicios;
import pe.edu.upc.femfitai.entities.SesionesEntrenamiento;
import pe.edu.upc.femfitai.repositories.DetalleSesionRepository;
import pe.edu.upc.femfitai.repositories.RutinaEjerciciosRepository;
import pe.edu.upc.femfitai.repositories.SesionesEntrenamientoRepository;
import pe.edu.upc.femfitai.repositories.RutinasRepository;
import pe.edu.upc.femfitai.repositories.UsuariosRepository;

@Service
public class SesionesEntrenamientoService implements ISesionesEntrenamientoService {
    private final SesionesEntrenamientoRepository repository;
    private final RutinasRepository rutinasRepository;
    private final UsuariosRepository usuariosRepository;
    private final UsuarioActualService actual;
    private final RutinaEjerciciosRepository rutinaEjerciciosRepository;
    private final DetalleSesionRepository detalleSesionRepository;

    public SesionesEntrenamientoService(SesionesEntrenamientoRepository repository,
                                        RutinasRepository rutinasRepository,
                                        UsuariosRepository usuariosRepository,
                                        UsuarioActualService actual,
                                        RutinaEjerciciosRepository rutinaEjerciciosRepository,
                                        DetalleSesionRepository detalleSesionRepository) {
        this.repository = repository;
        this.rutinasRepository = rutinasRepository;
        this.usuariosRepository = usuariosRepository;
        this.actual = actual;
        this.rutinaEjerciciosRepository = rutinaEjerciciosRepository;
        this.detalleSesionRepository = detalleSesionRepository;
    }

    @Override
    @Transactional
    public SesionesEntrenamientoDTO registrar(SesionesEntrenamientoDTO datos) {
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                "Las sesiones nuevas deben iniciarse desde una rutina planificada con POST /sesiones/rutina/{idRutina}/iniciar");
    }

    @Override
    @Transactional
    public SesionesEntrenamientoDTO iniciarDesdeRutina(Integer idRutina) {
        if (idRutina == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IdRutina es obligatorio");
        }
        if (idRutina <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IdRutina debe ser positivo");
        }

        Rutinas rutina = rutinasRepository.findById(idRutina)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe una rutina con ID " + idRutina));
        actual.verificar(rutina.getUsuario().getIdUsuario());
        if (Boolean.FALSE.equals(rutina.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La rutina no está activa");
        }

        Integer idUsuario = actual.id();
        boolean existeActiva = repository.existsByUsuario_IdUsuarioAndRutina_IdRutinaAndEstado(
                idUsuario, idRutina, "EN_CURSO");
        if (existeActiva) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una sesión en curso para esta rutina");
        }

        Usuarios usuario = usuariosRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Usuario autenticado no disponible"));

        SesionesEntrenamiento nueva = new SesionesEntrenamiento(
                rutina,
                usuario,
                LocalDateTime.now(),
                null,
                null,
                null,
                "EN_CURSO");
        SesionesEntrenamiento guardada = repository.save(nueva);

        List<RutinaEjercicios> ejerciciosRutina = rutinaEjerciciosRepository
                .findByRutina_IdRutinaOrderByIdRutinaEjercicioAsc(idRutina);

        for (RutinaEjercicios rutinaEjercicio : ejerciciosRutina) {
            Integer idEjercicio = rutinaEjercicio.getEjercicio().getIdEjercicio();
            if (detalleSesionRepository.existsBySesion_IdSesionAndEjercicio_IdEjercicio(
                    guardada.getIdSesion(), idEjercicio)) {
                continue;
            }
            detalleSesionRepository.save(new DetalleSesion(guardada, rutinaEjercicio.getEjercicio(), null));
        }

        return convertirADTO(guardada);
    }

    @Override
    @Transactional
    public SesionesEntrenamientoDTO finalizar(Integer id, FinalizarSesionDTO datos) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IdSesion es obligatorio");
        }
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IdSesion debe ser positivo");
        }
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los datos de finalizacion son obligatorios");
        }

        SesionesEntrenamiento sesion = obtenerSesion(id);
        actual.verificar(sesion.getUsuario().getIdUsuario());

        if (!"EN_CURSO".equalsIgnoreCase(sesion.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La sesion ya no se encuentra en curso");
        }
        if (datos.getDuracionMin() == null || datos.getDuracionMin() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "DuracionMin debe ser un valor positivo");
        }
        if (datos.getNivelEnergia() == null || datos.getNivelEnergia() < 1 || datos.getNivelEnergia() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "NivelEnergia debe estar entre 1 y 5");
        }
        if (datos.getEsfuerzoPercibido() == null || datos.getEsfuerzoPercibido() < 1 || datos.getEsfuerzoPercibido() > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "EsfuerzoPercibido debe estar entre 1 y 10");
        }

        sesion.setDuracionMin(datos.getDuracionMin());
        sesion.setNivelEnergia(datos.getNivelEnergia());
        sesion.setEsfuerzoPercibido(datos.getEsfuerzoPercibido());
        sesion.setEstado("FINALIZADA");

        return convertirADTO(repository.save(sesion));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SesionesEntrenamientoDTO> listar() {
        return repository.findByUsuario_IdUsuario(actual.id()).stream().map(this::convertirADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SesionesEntrenamientoDTO buscarPorId(Integer id) {
        SesionesEntrenamiento sesion = obtenerSesion(id);
        actual.verificar(sesion.getUsuario().getIdUsuario());
        return convertirADTO(sesion);
    }

    @Override
    @Transactional
    public SesionesEntrenamientoDTO actualizar(Integer id, SesionesEntrenamientoDTO datos) {
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                "La sesion no puede modificarse mediante este endpoint. Use PUT /sesiones/{id}/finalizar para cerrar una sesion EN_CURSO");
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                "La sesion no puede eliminarse por historial. Mantenga el registro de entrenamiento");
    }

    @Override
    @Transactional(readOnly = true)
    public List<SesionesEntrenamientoDTO> listarPorUsuario(Integer idUsuario) {
        actual.verificar(idUsuario);
        return repository.findByUsuario_IdUsuario(idUsuario).stream().map(this::convertirADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SesionesEntrenamientoDTODetalle buscarDetallePorId(Integer id) {
        if (id == null) {
            throw noEncontrada(id);
        }
        SesionesEntrenamiento sesion = obtenerSesion(id);
        actual.verificar(sesion.getUsuario().getIdUsuario());
        return repository.buscarDetallePorId(id).orElseThrow(() -> noEncontrada(id));
    }



    private ResponseStatusException noEncontrada(Integer id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No existe una sesion con ID " + id);
    }

    private void validarRegistro(SesionesEntrenamientoDTO datos) {
        if (datos.getIdRutina() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IdRutina es obligatorio");
        var rutina = rutinasRepository.findById(datos.getIdRutina()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe una rutina con ID " + datos.getIdRutina()));
        actual.verificar(rutina.getUsuario().getIdUsuario());
        if (datos.getIdUsuario() != null) actual.verificar(datos.getIdUsuario());
        Validaciones.texto(datos.getEstado(), 30, "Estado", false);
        Validaciones.positivo(datos.getDuracionMin(), "DuracionMin");
        if (datos.getNivelEnergia() == null || datos.getNivelEnergia() < 1 || datos.getNivelEnergia() > 5)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NivelEnergia debe estar entre 1 y 5");
        if (datos.getEsfuerzoPercibido() == null || datos.getEsfuerzoPercibido() < 1 || datos.getEsfuerzoPercibido() > 10)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "EsfuerzoPercibido debe estar entre 1 y 10");
    }

    private void validar(Integer idRutina, Integer idUsuario) {
        if (idRutina == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IdRutina es obligatorio");
        }
        if (idUsuario == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IdUsuario es obligatorio");
        }
        if (!rutinasRepository.existsById(idRutina)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No existe una rutina con ID " + idRutina);
        }
        if (!usuariosRepository.existsById(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No existe un usuario con ID " + idUsuario);
        }
    }

    private SesionesEntrenamiento obtenerSesion(Integer id) {
        if (id == null) {
            throw noEncontrada(id);
        }
        return repository.findById(id).orElseThrow(() -> noEncontrada(id));
    }



    private Rutinas obtenerRutina(Integer idRutina) {
        if (idRutina == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IdRutina es obligatorio");
        }
        return rutinasRepository.findById(idRutina)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No existe una rutina con ID " + idRutina));
    }

    private Usuarios obtenerUsuario(Integer idUsuario) {
        if (idUsuario == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IdUsuario es obligatorio");
        }
        return usuariosRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No existe un usuario con ID " + idUsuario));
    }

    private SesionesEntrenamientoDTO convertirADTO(SesionesEntrenamiento sesion) {
        return new SesionesEntrenamientoDTO(
                sesion.getIdSesion(), sesion.getRutina().getIdRutina(), sesion.getUsuario().getIdUsuario(),
                sesion.getFecha(), sesion.getDuracionMin(), sesion.getNivelEnergia(),
                sesion.getEsfuerzoPercibido(), sesion.getEstado());
    }
}
