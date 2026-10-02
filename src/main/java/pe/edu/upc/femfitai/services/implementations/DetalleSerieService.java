package pe.edu.upc.femfitai.services.implementations;

import java.util.*;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.femfitai.dtos.*;
import pe.edu.upc.femfitai.entities.*;
import pe.edu.upc.femfitai.repositories.*;
import pe.edu.upc.femfitai.services.interfaces.IDetalleSerieService;
import static pe.edu.upc.femfitai.services.implementations.Validaciones.*;

@Service
public class DetalleSerieService implements IDetalleSerieService {
    private final DetalleSerieRepository repository;
    private final DetalleSesionRepository detalles;
    private final SesionesEntrenamientoRepository sesiones;
    private final UsuarioActualService actual;
    public DetalleSerieService(DetalleSerieRepository repository, DetalleSesionRepository detalles, SesionesEntrenamientoRepository sesiones, UsuarioActualService actual) {
        this.repository = repository;
        this.detalles = detalles;
        this.sesiones = sesiones;
        this.actual = actual;
    }
    @Override
    @Transactional
    public DetalleSerieDTO registrar(DetalleSerieDTO d) {
        exigir(d != null, "Los datos son obligatorios");
        positivo(d.idDetalle(), "IdDetalle");
        var detalle = detalles.bloquear(d.idDetalle()).orElseThrow(() -> noEncontrado("Detalle de sesion"));
        propietario(detalle);
        positivo(d.numeroSerie(), "NumeroSerie");
        validar(d.repeticiones(), d.pesoKg());
        conflicto(repository.existsByIdDetalleAndNumeroSerie(d.idDetalle(), d.numeroSerie()), "NumeroSerie ya registrado para este ejercicio");
        // 2. Creamos la entidad pasando el objeto completo en lugar del Integer suelto
        DetalleSerie nuevaSerie = new DetalleSerie(
                detalle,
                d.numeroSerie(),
                d.repeticiones(),
                d.pesoKg()
        );

        return dto(repository.save(nuevaSerie));
    }
    @Transactional(readOnly = true)
    public List<DetalleSerieDTO> listarPorDetalle(Integer idDetalle) {
        detallePropio(idDetalle);
        return repository.findByIdDetalleOrderByNumeroSerieAscIdSerieAsc(idDetalle).stream().map(this::dto).toList();
    }
    @Transactional
    public DetalleSerieDTO actualizar(Integer id, ActualizarSerieDTO d) {
        exigir(d != null, "Los datos son obligatorios");
        var e = propia(id);
        validar(d.repeticiones(), d.pesoKg());
        e.setRepeticiones(d.repeticiones()); e.setPesoKg(d.pesoKg());
        return dto(repository.save(e));
    }
    @Transactional
    public void eliminar(Integer id) { repository.delete(propia(id)); }
    private DetalleSerie propia(Integer id) {
        positivo(id, "IdSerie");
        var e = repository.findById(id).orElseThrow(() -> noEncontrado("Serie"));
        // Navegamos por el objeto DetalleSesion para obtener el ID
        detallePropio(e.getDetalle().getIdDetalle());        return e;
    }
    private void detallePropio(Integer id) {
        positivo(id, "IdDetalle");
        propietario(detalles.findById(id).orElseThrow(() -> noEncontrado("Detalle de sesion")));
    }
    private void propietario(DetalleSesion d) {
        // Navegamos por el objeto Sesion, y luego por el objeto Usuario
        actual.verificar(sesiones.findById(d.getSesion().getIdSesion())
                .orElseThrow(() -> noEncontrado("Sesion")).getUsuario().getIdUsuario());
    }
    private void validar(Integer repeticiones, BigDecimal peso) {
        positivo(repeticiones, "Repeticiones");
        decimal(peso, "PesoKg", true, true);
    }
    private DetalleSerieDTO dto(DetalleSerie e) {
        // CORRECCIÓN: Usamos getDetalle() tal como descubriste arriba
        Integer idDetalle = (e.getDetalle() != null) ? e.getDetalle().getIdDetalle() : null;

        return new DetalleSerieDTO(
                e.getIdSerie(),
                idDetalle,
                e.getNumeroSerie(),
                e.getRepeticiones(),
                e.getPesoKg()
        );
    }
}
