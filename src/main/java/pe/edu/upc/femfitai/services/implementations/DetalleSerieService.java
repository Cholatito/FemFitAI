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
    @Transactional
    public DetalleSerieDTO registrar(DetalleSerieDTO d) {
        exigir(d != null, "Los datos son obligatorios");
        positivo(d.idDetalle(), "IdDetalle");
        var detalle = detalles.bloquear(d.idDetalle()).orElseThrow(() -> noEncontrado("Detalle de sesion"));
        propietario(detalle);
        positivo(d.numeroSerie(), "NumeroSerie");
        validar(d.repeticiones(), d.pesoKg());
        conflicto(repository.existsByIdDetalleAndNumeroSerie(d.idDetalle(), d.numeroSerie()), "NumeroSerie ya registrado para este ejercicio");
        return dto(repository.save(new DetalleSerie(d.idDetalle(), d.numeroSerie(), d.repeticiones(), d.pesoKg())));
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
        detallePropio(e.getIdDetalle());
        return e;
    }
    private void detallePropio(Integer id) {
        positivo(id, "IdDetalle");
        propietario(detalles.findById(id).orElseThrow(() -> noEncontrado("Detalle de sesion")));
    }
    private void propietario(DetalleSesion d) {
        actual.verificar(sesiones.findById(d.getIdSesion()).orElseThrow(() -> noEncontrado("Sesion")).getIdUsuario());
    }
    private void validar(Integer repeticiones, BigDecimal peso) {
        positivo(repeticiones, "Repeticiones");
        decimal(peso, "PesoKg", true, true);
    }
    private DetalleSerieDTO dto(DetalleSerie e) {
        return new DetalleSerieDTO(e.getIdSerie(), e.getIdDetalle(), e.getNumeroSerie(), e.getRepeticiones(), e.getPesoKg());
    }
}
