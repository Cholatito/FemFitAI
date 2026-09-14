package pe.edu.upc.femfitai.services.implementations;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.femfitai.dtos.EjerciciosDTOInsert;
import pe.edu.upc.femfitai.dtos.EjerciciosDTOList;
import pe.edu.upc.femfitai.dtos.EjerciciosUsuarioDTO;
import pe.edu.upc.femfitai.entities.Ejercicios;
import pe.edu.upc.femfitai.repositories.EjerciciosRepository;
import pe.edu.upc.femfitai.services.interfaces.IEjerciciosService;

@Service
public class EjerciciosService implements IEjerciciosService {
    private final EjerciciosRepository repository;

    public EjerciciosService(EjerciciosRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public EjerciciosDTOList registrar(EjerciciosDTOInsert datos) {
        if (datos == null || datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre es obligatorio");
        }
        validarLongitud(datos.getNombre(), 100, "Nombre");
        validarLongitud(datos.getGrupoMuscular(), 100, "GrupoMuscular");
        validarLongitud(datos.getTipo(), 50, "Tipo");
        Ejercicios ejercicio = new Ejercicios(datos.getNombre(), datos.getGrupoMuscular(),
                datos.getTipo(), datos.getDescripcion());
        return convertirADTO(repository.save(ejercicio));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EjerciciosDTOList> listar() {
        return repository.findAll().stream().map(this::convertirADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EjerciciosDTOList buscarPorId(Integer id) {
        return convertirADTO(obtenerEjercicio(id));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Ejercicios ejercicio = obtenerEjercicio(id);
        try {
            repository.delete(ejercicio);
            // Ejecutar el DELETE dentro del try para capturar las restricciones de integridad.
            repository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar el ejercicio porque tiene registros relacionados", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EjerciciosDTOList> buscarPorGrupoMuscular(String grupoMuscular) {
        return repository.buscarPorGrupoMuscular(grupoMuscular).stream()
                .map(this::convertirADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EjerciciosUsuarioDTO> listarPorUsuario(Integer idUsuario) {
        return repository.listarPorUsuario(idUsuario);
    }

    private Ejercicios obtenerEjercicio(Integer id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un ejercicio con ID " + id);
        }
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un ejercicio con ID " + id));
    }

    private void validarLongitud(String valor, int maximo, String campo) {
        if (valor != null && valor.length() > maximo) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    campo + " no debe superar " + maximo + " caracteres");
        }
    }

    private EjerciciosDTOList convertirADTO(Ejercicios ejercicio) {
        return new EjerciciosDTOList(ejercicio.getIdEjercicio(), ejercicio.getNombre(),
                ejercicio.getGrupoMuscular(), ejercicio.getTipo(), ejercicio.getDescripcion());
    }
}
