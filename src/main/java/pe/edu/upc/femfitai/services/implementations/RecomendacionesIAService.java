package pe.edu.upc.femfitai.services.implementations;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.femfitai.dtos.RecomendacionesIADTO;
import pe.edu.upc.femfitai.entities.RecomendacionesIA;
import pe.edu.upc.femfitai.repositories.RecomendacionesIARepository;
import pe.edu.upc.femfitai.services.interfaces.IRecomendacionesIAService;

import static pe.edu.upc.femfitai.services.implementations.Validaciones.*;

@Service
public class RecomendacionesIAService implements IRecomendacionesIAService {
    private final RecomendacionesIARepository repository;
    private final UsuarioActualService actual;

    public RecomendacionesIAService(RecomendacionesIARepository repository, UsuarioActualService actual) {
        this.repository = repository;
        this.actual = actual;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecomendacionesIADTO> listar(int pagina, int tamano) {
        return repository.findByUsuario_IdUsuarioOrderByFechaDescIdRecomendacionDesc(actual.id(),
                paginar(pagina, tamano)).map(this::dto);
    }

    @Override
    @Transactional(readOnly = true)
    public RecomendacionesIADTO buscarPorId(Integer id) {
        return dto(propia(id));
    }

    @Override
    @Transactional
    public RecomendacionesIADTO aceptar(Integer id) {
        RecomendacionesIA recomendacion = propia(id);
        // Repetir la aceptación no modifica el contenido ni la fecha de generación.
        if (!Boolean.TRUE.equals(recomendacion.getAceptada())) {
            recomendacion.setAceptada(true);
            repository.save(recomendacion);
        }
        return dto(recomendacion);
    }

    private RecomendacionesIA propia(Integer id) {
        positivo(id, "IdRecomendacion");
        RecomendacionesIA recomendacion = repository.findById(id)
                .orElseThrow(() -> noEncontrado("Recomendacion"));

        // CORRECCIÓN: Navegamos por el objeto usuario
        actual.verificar(recomendacion.getUsuario().getIdUsuario());

        return recomendacion;
    }

    private RecomendacionesIADTO dto(RecomendacionesIA e) {
        // CORRECCIÓN: Extraemos los IDs de forma segura desde los objetos
        Integer idUsuario = (e.getUsuario() != null) ? e.getUsuario().getIdUsuario() : null;
        Integer idRutina = (e.getRutina() != null) ? e.getRutina().getIdRutina() : null;

        return new RecomendacionesIADTO(
                e.getIdRecomendacion(),
                idUsuario,
                idRutina,
                e.getFecha(), // Recuerda usar el nombre correcto del getter de tu fecha, como vimos antes
                e.getTipo(),
                e.getContenido(),
                e.getMotivo(),
                e.getAceptada()
        );
    }
}
