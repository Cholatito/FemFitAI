package pe.edu.upc.femfitai.services.implementations;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.femfitai.dtos.DetalleDiarioCicloDTO;
import pe.edu.upc.femfitai.dtos.DetalleDiarioCicloRequestDTO;
import pe.edu.upc.femfitai.entities.Ciclos;
import pe.edu.upc.femfitai.entities.DetalleDiarioCiclo;
import pe.edu.upc.femfitai.repositories.CiclosRepository;
import pe.edu.upc.femfitai.repositories.DetalleDiarioCicloRepository;
import pe.edu.upc.femfitai.services.interfaces.IDetalleDiarioCicloService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DetalleDiarioCicloService implements IDetalleDiarioCicloService {


    private final DetalleDiarioCicloRepository repository;
    private final CiclosRepository ciclosRepository;

    public DetalleDiarioCicloService(DetalleDiarioCicloRepository repository,
                                     CiclosRepository ciclosRepository) {
        this.repository = repository;
        this.ciclosRepository = ciclosRepository;
    }

    @Override
    @Transactional
    public DetalleDiarioCicloDTO registrar(DetalleDiarioCicloDTO datos) {
        Ciclos ciclo = validar(datos);

        DetalleDiarioCiclo detalle = new DetalleDiarioCiclo(ciclo, datos.getFecha(),
                datos.getFaseRegistrada(), datos.getNivelEnergia(), datos.getObservaciones());
        return convertirADTO(repository.save(detalle));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetalleDiarioCicloDTO> listar() {
        return repository.findAll().stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DetalleDiarioCicloDTO buscarPorId(Integer id) {
        return convertirADTO(obtenerDetalle(id));
    }

    @Override
    @Transactional
    public DetalleDiarioCicloDTO actualizar(Integer id, DetalleDiarioCicloDTO datos) {
        DetalleDiarioCiclo detalle = obtenerDetalle(id);
        Ciclos ciclo = validar(datos);

        detalle.setCiclo(ciclo);
        detalle.setFecha(datos.getFecha());
        detalle.setFaseRegistrada(datos.getFaseRegistrada());
        detalle.setNivelEnergia(datos.getNivelEnergia());
        detalle.setObservaciones(datos.getObservaciones());
        return convertirADTO(repository.save(detalle));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        repository.delete(obtenerDetalle(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetalleDiarioCicloDTO> listarPorCiclo(Long idCiclo) {
        if (!esInteger(idCiclo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "idCiclo debe estar dentro del rango INTEGER de PostgreSQL");
        }
        return repository.findByCiclo_IdCicloOrderByFechaAsc(idCiclo).stream()
                .map(this::convertirADTO)
                .toList();
    }

    // ---------- helpers privados ----------

    private DetalleDiarioCiclo obtenerDetalle(Integer id) {
        if (id == null) {
            throw noEncontrado(id);
        }
        return repository.findById(id).orElseThrow(() -> noEncontrado(id));
    }

    private ResponseStatusException noEncontrado(Integer id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No existe un detalle diario de ciclo con ID " + id);
    }

    /** Valida el request y devuelve el ciclo (FK) ya cargado desde la BD. */
    private Ciclos validar(DetalleDiarioCicloDTO datos) {
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos son obligatorios");
        }
        Validaciones.exigir(datos.getIdCiclo() != null, "idCiclo es obligatorio");
        Validaciones.exigir(datos.getFecha() != null, "fecha es obligatoria");
        Validaciones.texto(datos.getFaseRegistrada(), 50, "faseRegistrada", false);
        Validaciones.texto(datos.getObservaciones(), 255, "observaciones", false);

        if (!esInteger(datos.getIdCiclo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "idCiclo debe estar dentro del rango INTEGER de PostgreSQL");
        }
        return ciclosRepository.findById(datos.getIdCiclo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No existe un ciclo con ID " + datos.getIdCiclo()));
    }

    private boolean esInteger(Long valor) {
        return valor != null && valor >= Integer.MIN_VALUE && valor <= Integer.MAX_VALUE;
    }

    private DetalleDiarioCicloDTO convertirADTO(DetalleDiarioCiclo detalle) {
        return new DetalleDiarioCicloDTO(detalle.getIdDetalleDiarioCiclo(),
                detalle.getCiclo().getIdCiclo(), detalle.getFecha(),
                detalle.getFaseRegistrada(), detalle.getNivelEnergia(), detalle.getObservaciones());
    }
}

