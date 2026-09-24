package pe.edu.upc.femfitai.services.implementations;

import pe.edu.upc.femfitai.services.interfaces.IRutinasService;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.femfitai.dtos.RutinasDTO;
import pe.edu.upc.femfitai.dtos.RutinasUsuarioDTO;
import pe.edu.upc.femfitai.entities.Rutinas;
import pe.edu.upc.femfitai.repositories.RutinasRepository;
import pe.edu.upc.femfitai.repositories.UsuariosRepository;

@Service
public class RutinasService implements IRutinasService {
    private final RutinasRepository repository;
    private final UsuariosRepository usuariosRepository;
    private final UsuarioActualService actual;

    public RutinasService(RutinasRepository repository, UsuariosRepository usuariosRepository, UsuarioActualService actual) {
        this.repository = repository;
        this.usuariosRepository = usuariosRepository;
        this.actual = actual;
    }

    @Override
    @Transactional
    public RutinasDTO registrar(RutinasDTO datos) {
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos son obligatorios");
        }
        validar(datos.getIdUsuario(), datos.getNombre());
        Rutinas rutina = new Rutinas(datos.getIdUsuario(), datos.getNombre(),
                datos.getObjetivo(), datos.getNivel(),
                datos.getFechaCreacion() == null ? LocalDateTime.now() : datos.getFechaCreacion(),
                datos.getEstado() == null ? true : datos.getEstado());
        return convertirADTO(repository.save(rutina));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RutinasDTO> listar() {
        return repository.findByIdUsuario(actual.id()).stream().map(this::convertirADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RutinasDTO buscarPorId(Integer id) {
        return convertirADTO(obtenerRutina(id));
    }

    @Override
    @Transactional
    public RutinasDTO actualizar(Integer id, RutinasDTO datos) {
        Rutinas rutina = obtenerRutina(id);
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos son obligatorios");
        }
        validar(datos.getIdUsuario(), datos.getNombre());
        rutina.setIdUsuario(datos.getIdUsuario());
        rutina.setNombre(datos.getNombre());
        rutina.setObjetivo(datos.getObjetivo());
        rutina.setNivel(datos.getNivel());
        rutina.setFechaCreacion(datos.getFechaCreacion());
        rutina.setEstado(datos.getEstado());
        return convertirADTO(repository.save(rutina));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        repository.delete(obtenerRutina(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RutinasDTO> listarPorUsuario(Integer idUsuario) {
        actual.verificar(idUsuario);
        return repository.findByIdUsuario(idUsuario).stream().map(this::convertirADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RutinasUsuarioDTO buscarDetallePorId(Integer id) {
        if (id == null) {
            throw noEncontrada(id);
        }
        obtenerRutina(id);
        return repository.buscarDetallePorId(id).orElseThrow(() -> noEncontrada(id));
    }

    private Rutinas obtenerRutina(Integer id) {
        if (id == null) {
            throw noEncontrada(id);
        }
        Rutinas rutina = repository.findById(id).orElseThrow(() -> noEncontrada(id));
        actual.verificar(rutina.getIdUsuario());
        return rutina;
    }

    private ResponseStatusException noEncontrada(Integer id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No existe una rutina con ID " + id);
    }

    private void validar(Integer idUsuario, String nombre) {
        if (idUsuario == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IdUsuario es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre es obligatorio");
        }
        actual.verificar(idUsuario);
        if (!usuariosRepository.existsById(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No existe un usuario con ID " + idUsuario);
        }
    }

    private RutinasDTO convertirADTO(Rutinas rutina) {
        return new RutinasDTO(rutina.getIdRutina(), rutina.getIdUsuario(),
                rutina.getNombre(), rutina.getObjetivo(), rutina.getNivel(),
                rutina.getFechaCreacion(), rutina.getEstado());
    }
}