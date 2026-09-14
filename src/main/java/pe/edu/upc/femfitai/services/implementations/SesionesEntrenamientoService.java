package pe.edu.upc.femfitai.services.implementations;

import pe.edu.upc.femfitai.services.interfaces.ISesionesEntrenamientoService;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTOInsert;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTOUpdate;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTOList;
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

    public SesionesEntrenamientoService(SesionesEntrenamientoRepository repository,
                                        RutinasRepository rutinasRepository,
                                        UsuariosRepository usuariosRepository) {
        this.repository = repository;
        this.rutinasRepository = rutinasRepository;
        this.usuariosRepository = usuariosRepository;
    }

    @Override
    @Transactional
    public SesionesEntrenamientoDTOList registrar(SesionesEntrenamientoDTOInsert datos) {
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos son obligatorios");
        }
        validar(datos.getIdRutina(), datos.getIdUsuario());
        SesionesEntrenamiento sesion = new SesionesEntrenamiento(
                datos.getIdRutina(), datos.getIdUsuario(),
                datos.getFecha() == null ? LocalDateTime.now() : datos.getFecha(),
                datos.getDuracionMin(), datos.getNivelEnergia(),
                datos.getEsfuerzoPercibido(), datos.getEstado());
        return convertirADTO(repository.save(sesion));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SesionesEntrenamientoDTOList> listar() {
        return repository.findAll().stream().map(this::convertirADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SesionesEntrenamientoDTOList buscarPorId(Integer id) {
        return convertirADTO(obtenerSesion(id));
    }

    @Override
    @Transactional
    public SesionesEntrenamientoDTOList actualizar(Integer id, SesionesEntrenamientoDTOUpdate datos) {
        SesionesEntrenamiento sesion = obtenerSesion(id);
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos son obligatorios");
        }
        validar(datos.getIdRutina(), datos.getIdUsuario());
        sesion.setIdRutina(datos.getIdRutina());
        sesion.setIdUsuario(datos.getIdUsuario());
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
    public List<SesionesEntrenamientoDTOList> listarPorUsuario(Integer idUsuario) {
        return repository.findByIdUsuario(idUsuario).stream().map(this::convertirADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SesionesEntrenamientoDTODetalle buscarDetallePorId(Integer id) {
        if (id == null) {
            throw noEncontrada(id);
        }
        return repository.buscarDetallePorId(id).orElseThrow(() -> noEncontrada(id));
    }

    private SesionesEntrenamiento obtenerSesion(Integer id) {
        if (id == null) {
            throw noEncontrada(id);
        }
        return repository.findById(id).orElseThrow(() -> noEncontrada(id));
    }

    private ResponseStatusException noEncontrada(Integer id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No existe una sesion con ID " + id);
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

    private SesionesEntrenamientoDTOList convertirADTO(SesionesEntrenamiento sesion) {
        return new SesionesEntrenamientoDTOList(
                sesion.getIdSesion(), sesion.getIdRutina(), sesion.getIdUsuario(),
                sesion.getFecha(), sesion.getDuracionMin(), sesion.getNivelEnergia(),
                sesion.getEsfuerzoPercibido(), sesion.getEstado());
    }
}
