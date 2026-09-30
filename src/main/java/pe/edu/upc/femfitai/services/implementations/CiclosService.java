package pe.edu.upc.femfitai.services.implementations;

import pe.edu.upc.femfitai.services.interfaces.ICiclosService;

import pe.edu.upc.femfitai.dtos.CiclosUsuarioDTO;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

import pe.edu.upc.femfitai.dtos.CiclosDTO;
import pe.edu.upc.femfitai.dtos.CiclosRequestDTO;
import pe.edu.upc.femfitai.entities.Ciclos;
import pe.edu.upc.femfitai.entities.Usuarios;
import pe.edu.upc.femfitai.repositories.CiclosRepository;
import pe.edu.upc.femfitai.repositories.UsuariosRepository;

@Service
public class CiclosService implements ICiclosService {

    private final CiclosRepository repository;
    private final UsuariosRepository usuariosRepository;

    public CiclosService(CiclosRepository repository, UsuariosRepository usuariosRepository) {
        this.repository = repository;
        this.usuariosRepository = usuariosRepository;
    }

    @Override
    @Transactional
    public CiclosDTO registrar(CiclosRequestDTO datos) {
        if (datos == null || datos.getIdUsuario() == null || datos.getFechaInicio() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "idUsuario y fechaInicio son obligatorios");
        }

        Ciclos ciclo = new Ciclos();
        ciclo.setUsuario(obtenerUsuario(datos.getIdUsuario()));
        ciclo.setFechaInicio(datos.getFechaInicio());
        ciclo.setFechaFinEstimada(datos.getFechaFinEstimada());
        ciclo.setFechaReal(datos.getFechaReal());
        return convertirADTO(repository.save(ciclo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CiclosDTO> listar() {
        return repository.findAll().stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CiclosDTO buscarPorId(Long id) {
        return convertirADTO(obtenerCiclo(id));
    }

    @Override
    @Transactional
    public CiclosDTO actualizar(Long id, CiclosRequestDTO datos) {
        Ciclos ciclo = obtenerCiclo(id);
        if (datos == null || datos.getIdUsuario() == null || datos.getFechaInicio() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "idUsuario y fechaInicio son obligatorios");
        }

        ciclo.setUsuario(obtenerUsuario(datos.getIdUsuario()));
        ciclo.setFechaInicio(datos.getFechaInicio());
        ciclo.setFechaFinEstimada(datos.getFechaFinEstimada());
        ciclo.setFechaReal(datos.getFechaReal());
        return convertirADTO(repository.save(ciclo));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        repository.delete(obtenerCiclo(id));
    }

    private Ciclos obtenerCiclo(Long id) {
        if (id == null || !esInteger(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No existe un ciclo con ID " + id);
        }
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe un ciclo con ID " + id));
    }

    private boolean esInteger(Long valor) {
        return valor != null && valor >= Integer.MIN_VALUE && valor <= Integer.MAX_VALUE;
    }

    private Usuarios obtenerUsuario(Integer idUsuario) {
        return usuariosRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No existe un usuario con ID " + idUsuario));
    }

    private CiclosDTO convertirADTO(Ciclos ciclo) {
        return new CiclosDTO(ciclo.getIdCiclo(), ciclo.getUsuario().getIdUsuario(),
                ciclo.getFechaInicio(), ciclo.getFechaFinEstimada(), ciclo.getFechaReal());
    }

    public List<CiclosDTO> listarPorUsuario(Integer idUsuario) {
        return repository.findByUsuario_IdUsuario(idUsuario).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CiclosUsuarioDTO buscarDetallePorId(Long id) {
        if (id == null || !esInteger(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No existe un ciclo con ID " + id);
        }
        return repository.buscarDetallePorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe un ciclo con ID " + id));
    }
}
