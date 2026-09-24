package pe.edu.upc.femfitai.services.implementations;

import pe.edu.upc.femfitai.services.interfaces.ISesionesEntrenamientoService;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTO;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTODetalle;
import pe.edu.upc.femfitai.entities.SesionesEntrenamiento;
import pe.edu.upc.femfitai.repositories.SesionesEntrenamientoRepository;
import pe.edu.upc.femfitai.repositories.RutinasRepository;
import pe.edu.upc.femfitai.repositories.UsuariosRepository;

@Service
public class SesionesEntrenamientoService implements ISesionesEntrenamientoService {
    private final SesionesEntrenamientoRepository repository;
    private final RutinasRepository rutinasRepository;
    private final UsuariosRepository usuariosRepository;
    private final UsuarioActualService actual;

    public SesionesEntrenamientoService(SesionesEntrenamientoRepository repository,
                                        RutinasRepository rutinasRepository,
                                        UsuariosRepository usuariosRepository,
                                        UsuarioActualService actual) {
        this.repository = repository;
        this.rutinasRepository = rutinasRepository;
        this.usuariosRepository = usuariosRepository;
        this.actual = actual;
    }

    @Override
    @Transactional
    public SesionesEntrenamientoDTO registrar(SesionesEntrenamientoDTO datos) {
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos son obligatorios");
        }
        Integer idUsuario = actual.id();
        validarRegistro(datos);
        SesionesEntrenamiento sesion = new SesionesEntrenamiento(
                datos.getIdRutina(), idUsuario, LocalDateTime.now(),
                datos.getDuracionMin(), datos.getNivelEnergia(),
                datos.getEsfuerzoPercibido(), datos.getEstado());
        return convertirADTO(repository.save(sesion));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SesionesEntrenamientoDTO> listar() {
        return repository.findByIdUsuario(actual.id()).stream().map(this::convertirADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SesionesEntrenamientoDTO buscarPorId(Integer id) {
        return convertirADTO(obtenerSesion(id));
    }

    @Override
    @Transactional
    public SesionesEntrenamientoDTO actualizar(Integer id, SesionesEntrenamientoDTO datos) {
        SesionesEntrenamiento sesion = obtenerSesion(id);
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos son obligatorios");
        }
        validarRegistro(datos);
        sesion.setIdRutina(datos.getIdRutina());
        // Conservar propietario y fecha original de ejecucion.
        sesion.setDuracionMin(datos.getDuracionMin());
        sesion.setNivelEnergia(datos.getNivelEnergia());
        sesion.setEsfuerzoPercibido(datos.getEsfuerzoPercibido());
        sesion.setEstado(datos.getEstado());
        return convertirADTO(repository.save(sesion));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        repository.delete(obtenerSesion(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SesionesEntrenamientoDTO> listarPorUsuario(Integer idUsuario) {
        actual.verificar(idUsuario);
        return repository.findByIdUsuario(idUsuario).stream().map(this::convertirADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SesionesEntrenamientoDTODetalle buscarDetallePorId(Integer id) {
        if (id == null) {
            throw noEncontrada(id);
        }
        obtenerSesion(id);
        return repository.buscarDetallePorId(id).orElseThrow(() -> noEncontrada(id));
    }

    private SesionesEntrenamiento obtenerSesion(Integer id) {
        if (id == null) {
            throw noEncontrada(id);
        }
        SesionesEntrenamiento sesion = repository.findById(id).orElseThrow(() -> noEncontrada(id));
        actual.verificar(sesion.getIdUsuario());
        return sesion;
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
        actual.verificar(rutina.getIdUsuario());
        if (datos.getIdUsuario() != null) actual.verificar(datos.getIdUsuario());
        Validaciones.texto(datos.getEstado(), 30, "Estado", false);
        if (datos.getDuracionMin() == null || datos.getDuracionMin() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DuracionMin no puede ser negativa");
        // Escala RPE provisional 1..10: Trello exige validar escala pero no publica sus extremos.
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

    private SesionesEntrenamientoDTO convertirADTO(SesionesEntrenamiento sesion) {
        return new SesionesEntrenamientoDTO(
                sesion.getIdSesion(), sesion.getIdRutina(), sesion.getIdUsuario(),
                sesion.getFecha(), sesion.getDuracionMin(), sesion.getNivelEnergia(),
                sesion.getEsfuerzoPercibido(), sesion.getEstado());
    }
}
