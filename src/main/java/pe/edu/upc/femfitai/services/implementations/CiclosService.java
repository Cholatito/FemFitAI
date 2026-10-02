package pe.edu.upc.femfitai.services.implementations;

import pe.edu.upc.femfitai.services.interfaces.ICiclosService;

import pe.edu.upc.femfitai.dtos.CiclosUsuarioDTO;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.LocalDate;

import pe.edu.upc.femfitai.dtos.CiclosDTO;
import pe.edu.upc.femfitai.dtos.CiclosDTOUpdate;
import pe.edu.upc.femfitai.entities.Ciclos;
import pe.edu.upc.femfitai.repositories.CiclosRepository;

@Service
public class CiclosService implements ICiclosService {

    private final CiclosRepository repository;
    private final UsuarioActualService actual;
    private final pe.edu.upc.femfitai.repositories.UsuariosRepository usuarios;
    private final pe.edu.upc.femfitai.repositories.DetalleDiarioCicloRepository detalles;

    public CiclosService(CiclosRepository repository, UsuarioActualService actual,
                         pe.edu.upc.femfitai.repositories.UsuariosRepository usuarios,
                         pe.edu.upc.femfitai.repositories.DetalleDiarioCicloRepository detalles) {
        this.repository = repository;
        this.actual = actual;
        this.usuarios = usuarios;
        this.detalles = detalles;
    }

    @Override
    @Transactional
    public CiclosDTO registrar(CiclosDTO datos) {
        if (datos == null || datos.getIdUsuario() == null || datos.getFechaInicio() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "idUsuario y fechaInicio son obligatorios");
        }

        if (!esInteger(datos.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "idUsuario debe estar dentro del rango INTEGER de PostgreSQL");
        }

        LocalDate fechaFinEstimada = datos.getFechaInicio().plusDays(28);
        validarPropietarioYFechas(datos.getIdUsuario(), datos.getFechaInicio(), fechaFinEstimada, datos.getFechaReal());
        usuarios.bloquear(datos.getIdUsuario().intValue()).orElseThrow(() -> Validaciones.noEncontrado("Usuario"));
        Validaciones.conflicto(repository.existsByIdUsuarioAndFechaRealIsNull(datos.getIdUsuario()),
                "La usuaria ya tiene un ciclo activo");
        Ciclos ciclo = new Ciclos();
        ciclo.setIdUsuario(datos.getIdUsuario());
        ciclo.setFechaInicio(datos.getFechaInicio());
        ciclo.setFechaFinEstimada(fechaFinEstimada);
        ciclo.setFechaReal(datos.getFechaReal());
        return convertirADTO(repository.save(ciclo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CiclosDTO> listar() {
        return repository.findByIdUsuarioOrderByFechaInicioDescIdCicloDesc(actual.id().longValue()).stream()
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
    public CiclosDTO actualizar(Long id, CiclosDTOUpdate datos) {
        Ciclos ciclo = obtenerCiclo(id);
        if (datos == null || datos.getIdUsuario() == null || datos.getFechaInicio() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "idUsuario y fechaInicio son obligatorios");
        }

        if (!esInteger(datos.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "idUsuario debe estar dentro del rango INTEGER de PostgreSQL");
        }

        LocalDate fechaFinEstimada = datos.getFechaInicio().plusDays(28);
        validarPropietarioYFechas(datos.getIdUsuario(), datos.getFechaInicio(), fechaFinEstimada, datos.getFechaReal());
        usuarios.bloquear(datos.getIdUsuario().intValue()).orElseThrow(() -> Validaciones.noEncontrado("Usuario"));
        if (datos.getFechaReal() == null) {
            Validaciones.conflicto(repository.existsByIdUsuarioAndFechaRealIsNullAndIdCicloNot(datos.getIdUsuario(), id),
                    "La usuaria ya tiene otro ciclo activo");
        }
        ciclo.setIdUsuario(datos.getIdUsuario());
        ciclo.setFechaInicio(datos.getFechaInicio());
        ciclo.setFechaFinEstimada(fechaFinEstimada);
        ciclo.setFechaReal(datos.getFechaReal());
        return convertirADTO(repository.save(ciclo));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Ciclos ciclo = obtenerCiclo(id);
        Validaciones.conflicto(detalles.existsByIdCiclo(id.intValue()), "El ciclo tiene registros diarios relacionados");
        repository.delete(ciclo);
        repository.flush();
    }

    private Ciclos obtenerCiclo(Long id) {
        if (id == null || !esInteger(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No existe un ciclo con ID " + id);
        }
        Ciclos ciclo = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe un ciclo con ID " + id));
        actual.verificar(ciclo.getIdUsuario());
        return ciclo;
    }

    private boolean esInteger(Long valor) {
        return valor >= Integer.MIN_VALUE && valor <= Integer.MAX_VALUE;
    }

    private CiclosDTO convertirADTO(Ciclos ciclo) {
        return new CiclosDTO(ciclo.getIdCiclo(), ciclo.getIdUsuario(),
                ciclo.getFechaInicio(), ciclo.getFechaFinEstimada(), ciclo.getFechaReal());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CiclosDTO> listarPorUsuario(Long idUsuario) {
        actual.verificar(idUsuario);
        return repository.findByIdUsuarioOrderByFechaInicioDescIdCicloDesc(idUsuario).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CiclosUsuarioDTO buscarDetallePorId(Long id) {
        obtenerCiclo(id);
        if (id == null || !esInteger(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No existe un ciclo con ID " + id);
        }
        return repository.buscarDetallePorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe un ciclo con ID " + id));
    }

    private void validarPropietarioYFechas(Long usuario, java.time.LocalDate inicio,
                                           java.time.LocalDate estimada, java.time.LocalDate real) {
        actual.verificar(usuario);
        Validaciones.exigir(!inicio.isAfter(java.time.LocalDate.now()), "FechaInicio no puede ser futura");
        Validaciones.exigir(estimada == null || !estimada.isBefore(inicio), "FechaFinEstimada no puede ser anterior a FechaInicio");
        Validaciones.exigir(real == null || !real.isBefore(inicio), "FechaFinReal no puede ser anterior a FechaInicio");
    }
}
