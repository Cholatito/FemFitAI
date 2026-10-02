package pe.edu.upc.femfitai.services.implementations;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.femfitai.dtos.DetalleDiarioCicloDTO;
import pe.edu.upc.femfitai.entities.Ciclos;
import pe.edu.upc.femfitai.entities.DetalleDiarioCiclo;
import pe.edu.upc.femfitai.repositories.CiclosRepository;
import pe.edu.upc.femfitai.repositories.DetalleDiarioCicloRepository;
import pe.edu.upc.femfitai.services.interfaces.IDetalleDiarioCicloService;
import static pe.edu.upc.femfitai.services.implementations.Validaciones.*;

@Service
public class DetalleDiarioCicloService implements IDetalleDiarioCicloService {
    // La escala actual se conserva mientras se confirman los extremos funcionales en US08.
    private static final int ENERGIA_MIN = 1;
    private static final int ENERGIA_MAX = 5;

    private final DetalleDiarioCicloRepository repository;
    private final CiclosRepository ciclos;
    private final UsuarioActualService actual;
    private final pe.edu.upc.femfitai.repositories.UsuariosRepository usuarios;

    public DetalleDiarioCicloService(DetalleDiarioCicloRepository repository,
                                     CiclosRepository ciclos,
                                     UsuarioActualService actual,
                                     pe.edu.upc.femfitai.repositories.UsuariosRepository usuarios) {
        this.repository = repository;
        this.ciclos = ciclos;
        this.actual = actual;
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetalleDiarioCicloDTO> listar() {
        return repository.findAll().stream()
                .map(this::dto)
                .toList();
    }

    @Override
    public DetalleDiarioCicloDTO buscarPorId(Integer id) {
        return dto(obtenerDetalle(id));
    }

    @Override
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
    public void eliminar(Integer id) {
        repository.delete(obtenerDetalle(id));
    }

    @Override
    public List<DetalleDiarioCicloDTO> listarPorCiclo(Long idCiclo) {
        if (!esInteger(idCiclo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "idCiclo debe estar dentro del rango INTEGER de PostgreSQL");
        }
        return repository.findByCiclo_IdCicloOrderByFechaAsc(idCiclo).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional
    public DetalleDiarioCicloDTO guardar(DetalleDiarioCicloDTO d) {
        exigir(d != null, "Los datos son obligatorios");

        // Adaptamos el Long del DTO al Integer que espera tu helper 'positivo'
        positivo(d.getIdCiclo() != null ? d.getIdCiclo().intValue() : null, "IdCiclo");

        // Como getIdCiclo() ya devuelve un Long, lo pasamos directamente sin hacer .longValue()
        Ciclos ciclo = ciclos.findById(d.getIdCiclo())
                .orElseThrow(() -> Validaciones.noEncontrado("Ciclo"));
        actual.verificar(ciclo.getUsuario().getIdUsuario());
        LocalDate fecha = d.getFecha() == null ? LocalDate.now() : d.getFecha();
        exigir(!fecha.isAfter(LocalDate.now()), "Fecha no puede ser futura");
        exigir(d.getNivelEnergia() != null, "NivelEnergia es obligatorio");
        exigir(d.getNivelEnergia() >= ENERGIA_MIN && d.getNivelEnergia() <= ENERGIA_MAX,
                "NivelEnergia debe estar entre " + ENERGIA_MIN + " y " + ENERGIA_MAX);
        exigir(d.getFaseRegistrada() == null || java.util.Set.of("Menstrual", "Folicular", "Ovulatoria", "Lútea")
                .contains(d.getFaseRegistrada()), "FaseRegistrada debe ser Menstrual, Folicular, Ovulatoria o Lútea");
        texto(d.getObservaciones(), 255, "Observaciones", false);

        // US10: bloquear la usuaria antes de consultar evita inserciones concurrentes
        Integer usuario = actual.id();
        usuarios.bloquear(usuario).orElseThrow(() -> Validaciones.noEncontrado("Usuario"));

        DetalleDiarioCiclo e = repository.buscarPorUsuarioYFecha(usuario.longValue(), fecha)
                .stream().findFirst().orElseGet(DetalleDiarioCiclo::new);

        // Seteamos el objeto Ciclo completo
        e.setCiclo(ciclo);

        e.setFecha(fecha);
        e.setFaseRegistrada(d.getFaseRegistrada());
        e.setNivelEnergia(d.getNivelEnergia());
        e.setObservaciones(d.getObservaciones());

        return dto(repository.save(e));
    }
    private DetalleDiarioCicloDTO dto(DetalleDiarioCiclo e) {
        DetalleDiarioCicloDTO dto = new DetalleDiarioCicloDTO();

        dto.setIdDetalleDiarioCiclo(e.getIdDetalleDiarioCiclo());

        // Como tu DTO ahora espera un Long, extraemos el ID directamente sin convertirlo a intValue()
        Long cicloId = (e.getCiclo() != null) ? e.getCiclo().getIdCiclo() : null;
        dto.setIdCiclo(cicloId);

        dto.setFecha(e.getFecha());
        dto.setFaseRegistrada(e.getFaseRegistrada());
        dto.setNivelEnergia(e.getNivelEnergia());
        dto.setObservaciones(e.getObservaciones());

        return dto;
    }

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
        return ciclos.findById(datos.getIdCiclo())
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
