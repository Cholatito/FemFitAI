package pe.edu.upc.femfitai.services;

import pe.edu.upc.femfitai.dtos.CiclosUsuarioDTO;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import pe.edu.upc.femfitai.dtos.CiclosDTOInsert;
import pe.edu.upc.femfitai.dtos.CiclosDTOList;
import pe.edu.upc.femfitai.dtos.CiclosDTOUpdate;
import pe.edu.upc.femfitai.entities.Ciclos;
import pe.edu.upc.femfitai.repositories.CiclosRepository;

@Service
public class CiclosService implements ICiclosService {

    private final CiclosRepository repository;

    public CiclosService(CiclosRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public CiclosDTOList registrar(CiclosDTOInsert datos) {
        if (datos == null || datos.getIdUsuario() == null || datos.getFechaInicio() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "idUsuario y fechaInicio son obligatorios");
        }

        if (!esInteger(datos.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "idUsuario debe estar dentro del rango INTEGER de PostgreSQL");
        }

        Ciclos ciclo = new Ciclos();
        ciclo.setIdUsuario(datos.getIdUsuario());
        ciclo.setFechaInicio(datos.getFechaInicio());
        return convertirADTO(repository.save(ciclo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CiclosDTOList> listar() {
        return repository.findAll().stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CiclosDTOList buscarPorId(Long id) {
        return convertirADTO(obtenerCiclo(id));
    }

    @Override
    @Transactional
    public CiclosDTOList actualizar(Long id, CiclosDTOUpdate datos) {
        Ciclos ciclo = obtenerCiclo(id);
        if (datos == null || datos.getIdUsuario() == null || datos.getFechaInicio() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "idUsuario y fechaInicio son obligatorios");
        }

        if (!esInteger(datos.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "idUsuario debe estar dentro del rango INTEGER de PostgreSQL");
        }

        ciclo.setIdUsuario(datos.getIdUsuario());
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
        return valor >= Integer.MIN_VALUE && valor <= Integer.MAX_VALUE;
    }

    private CiclosDTOList convertirADTO(Ciclos ciclo) {
        return new CiclosDTOList(ciclo.getIdCiclo(), ciclo.getIdUsuario(),
                ciclo.getFechaInicio(), ciclo.getFechaFinEstimada(), ciclo.getFechaReal());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CiclosDTOList> listarPorUsuario(Long idUsuario) {
        return repository.findByIdUsuario(idUsuario).stream()
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
