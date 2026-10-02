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
        Rutinas rutina = obtenerRutina(datos.getIdRutina());
        Usuarios usuario = obtenerUsuario(datos.getIdUsuario());
        SesionesEntrenamiento sesion = new SesionesEntrenamiento(
                rutina, usuario,
                datos.getFecha() == null ? LocalDateTime.now() : datos.getFecha(),
                datos.getDuracionMin(), datos.getNivelEnergia(),
                datos.getEsfuerzoPercibido(), datos.getEstado());
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
        return convertirADTO(obtenerSesion(id));
    }

    @Override
    @Transactional
    public SesionesEntrenamientoDTO actualizar(Integer id, SesionesEntrenamientoDTO datos) {
        SesionesEntrenamiento sesion = obtenerSesion(id);
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos son obligatorios");
        }
        sesion.setRutina(obtenerRutina(datos.getIdRutina()));
        sesion.setUsuario(obtenerUsuario(datos.getIdUsuario()));
        sesion.setFecha(datos.getFecha());
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
        return repository.findByUsuario_IdUsuario(idUsuario).stream().map(this::convertirADTO).toList();
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
