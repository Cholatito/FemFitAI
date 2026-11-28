package pe.edu.upc.femfitai.services.implementations;
import java.util.Locale;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.femfitai.dtos.EjerciciosDTO;
import pe.edu.upc.femfitai.dtos.EjerciciosUsuarioDTO;
import pe.edu.upc.femfitai.entities.Ejercicios;
import pe.edu.upc.femfitai.repositories.EjerciciosRepository;
import pe.edu.upc.femfitai.services.interfaces.IEjerciciosService;

@Service
public class EjerciciosService implements IEjerciciosService {
    private static final Pattern CONTENIDO_PELIGROSO = Pattern.compile(
            "(?i)<\\s*(script|iframe|object|embed|svg)\\b|javascript\\s*:|\\bon[a-z]+\\s*=");
    private final EjerciciosRepository repository;
    private final UsuarioActualService actual;
    private final pe.edu.upc.femfitai.repositories.RutinaEjerciciosRepository rutinas;
    private final pe.edu.upc.femfitai.repositories.DetalleSesionRepository detalles;

    public EjerciciosService(EjerciciosRepository repository,
                             pe.edu.upc.femfitai.repositories.RutinaEjerciciosRepository rutinas,
                             pe.edu.upc.femfitai.repositories.DetalleSesionRepository detalles,
                             UsuarioActualService actual) {
        this.repository = repository;
        this.actual = actual;
        this.rutinas = rutinas;
        this.detalles = detalles;
    }

    @Override
    @Transactional
    public EjerciciosDTO registrar(EjerciciosDTO datos) {
        if (datos == null || datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre es obligatorio");
        }
        validarLongitud(datos.getNombre(), 100, "Nombre");
        validarLongitud(datos.getGrupoMuscular(), 100, "GrupoMuscular");
        validarLongitud(datos.getTipo(), 50, "Tipo");
        validarDescripcion(datos.getDescripcion());
        Ejercicios ejercicio = new Ejercicios(datos.getNombre(), datos.getGrupoMuscular(),
                datos.getTipo(), descripcionSegura(datos.getDescripcion()));
        ImagenEjercicio.asignar(ejercicio, datos.getImagenUrl(), datos.getImagenAutor(),
                datos.getImagenLicencia(), datos.getImagenLicenciaUrl());
        return convertirADTO(repository.save(ejercicio));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EjerciciosDTO> listar() {
        return repository.findAll().stream().map(this::convertirADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EjerciciosDTO buscarPorId(Integer id) {
        return convertirADTO(obtenerEjercicio(id));
    }

    @Override
    @Transactional
    public EjerciciosDTO actualizar(Integer id, EjerciciosDTO datos) {
        Ejercicios ejercicio = obtenerEjercicio(id);
        validarDatos(datos);
        ejercicio.setNombre(datos.getNombre());
        ejercicio.setGrupoMuscular(datos.getGrupoMuscular());
        ejercicio.setTipo(datos.getTipo());
        validarDescripcion(datos.getDescripcion());
        ejercicio.setDescripcion(descripcionSegura(datos.getDescripcion()));
        if (datos.isImagenIncluida()) {
            ImagenEjercicio.asignar(ejercicio, datos.getImagenUrl(), datos.getImagenAutor(),
                    datos.getImagenLicencia(), datos.getImagenLicenciaUrl());
        }
        return convertirADTO(repository.save(ejercicio));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Ejercicios ejercicio = obtenerEjercicio(id);
        Validaciones.conflicto(rutinas.existsByEjercicio_IdEjercicio(id) || detalles.existsByEjercicio_IdEjercicio(id),
                "El ejercicio tiene registros relacionados");
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
    public List<EjerciciosDTO> buscarPorGrupoMuscular(String grupoMuscular) {
        return repository.buscarPorGrupoMuscular(grupoMuscular).stream()
                .map(this::convertirADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EjerciciosUsuarioDTO> listarPorUsuario(Integer idUsuario) {
        actual.verificar(idUsuario);
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

    private void validarDatos(EjerciciosDTO datos) {
        if (datos == null || datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre es obligatorio");
        }
        validarLongitud(datos.getNombre(), 100, "Nombre");
        validarLongitud(datos.getGrupoMuscular(), 100, "GrupoMuscular");
        validarLongitud(datos.getTipo(), 50, "Tipo");
        validarDescripcion(datos.getDescripcion());
    }


    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<EjerciciosDTO> buscar(
            pe.edu.upc.femfitai.dtos.BusquedaEjerciciosDTO c) {

        // Validar que los criterios existan
        Validaciones.exigir(c != null, "Criterios requeridos");

        // Obtener la paginación
        var pagina = Validaciones.paginar(c.pagina(), c.tamano());

        // Normalizar el tipo
        String tipo = c.tipo() == null || c.tipo().isBlank()
                ? ""
                : c.tipo().trim().toLowerCase(Locale.ROOT);

        // Normalizar el texto de búsqueda
        String query = c.query() == null || c.query().isBlank()
                ? ""
                : "%" + c.query().trim()
                .toLowerCase(Locale.ROOT)
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_") + "%";

        // Consultar ejercicios
        return repository.buscar(
                tipo,
                query,
                org.springframework.data.domain.PageRequest.of(
                        pagina.getPageNumber(),
                        pagina.getPageSize(),
                        org.springframework.data.domain.Sort.by(
                                "nombre", "idEjercicio"
                        )
                )
        ).map(this::convertirADTO);

    }


    private void validarDescripcion(String valor) {
        if (valor != null && CONTENIDO_PELIGROSO.matcher(valor).find()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Descripcion contiene contenido no permitido");
        }
    }

    private String descripcionSegura(String valor) {
        return valor == null ? null : org.springframework.web.util.HtmlUtils.htmlEscape(valor);
    }

    private EjerciciosDTO convertirADTO(Ejercicios ejercicio) {
        EjerciciosDTO dto = new EjerciciosDTO(ejercicio.getIdEjercicio(), ejercicio.getNombre(),
                ejercicio.getGrupoMuscular(), ejercicio.getTipo(), ejercicio.getDescripcion());
        dto.setImagenUrl(ejercicio.getImagenUrl());
        dto.setImagenAutor(ejercicio.getImagenAutor());
        dto.setImagenLicencia(ejercicio.getImagenLicencia());
        dto.setImagenLicenciaUrl(ejercicio.getImagenLicenciaUrl());
        return dto;
    }
}
