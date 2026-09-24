package pe.edu.upc.femfitai.services.implementations;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.femfitai.dtos.*;
import pe.edu.upc.femfitai.entities.*;
import pe.edu.upc.femfitai.repositories.*;
import pe.edu.upc.femfitai.services.interfaces.IDetalleSesionService;
import static pe.edu.upc.femfitai.services.implementations.Validaciones.*;

@Service
public class DetalleSesionService implements IDetalleSesionService {
    private final DetalleSesionRepository repository;
    private final SesionesEntrenamientoRepository sesiones;
    private final EjerciciosRepository ejercicios;
    private final DetalleSerieRepository series;
    private final UsuarioActualService actual;
    public DetalleSesionService(DetalleSesionRepository repository, SesionesEntrenamientoRepository sesiones, EjerciciosRepository ejercicios, DetalleSerieRepository series, UsuarioActualService actual) {
        this.repository = repository;
        this.sesiones = sesiones;
        this.ejercicios = ejercicios;
        this.series = series;
        this.actual = actual;
    }
    @Transactional
    public DetalleSesionDTO registrar(DetalleSesionDTO d) {
        exigir(d != null, "Los datos son obligatorios");
        sesionPropia(d.idSesion());
        positivo(d.idEjercicio(), "IdEjercicio");
        if (!ejercicios.existsById(d.idEjercicio())) throw noEncontrado("Ejercicio");
        texto(d.observacion(), 255, "Observacion", false);
        return dto(repository.save(new DetalleSesion(d.idSesion(), d.idEjercicio(), d.observacion())));
    }
    @Transactional(readOnly = true)
    public List<DetalleSesionEjercicioDTO> listarPorSesion(Integer idSesion) {
        sesionPropia(idSesion);
        return repository.listarDetalle(idSesion);
    }
    @Transactional
    public DetalleSesionDTO actualizar(Integer id, ObservacionSesionDTO d) {
        exigir(d != null, "Los datos son obligatorios");
        texto(d.observacion(), 255, "Observacion", false);
        var e = propio(id);
        e.setObservacion(d.observacion());
        return dto(repository.save(e));
    }
    @Transactional
    public void eliminar(Integer id) {
        var e = propio(id);
        conflicto(series.existsByIdDetalle(id), "El ejercicio tiene series registradas; elimine las series antes");
        repository.delete(e);
        repository.flush();
    }
    private DetalleSesion propio(Integer id) {
        positivo(id, "IdDetalle");
        var e = repository.bloquear(id).orElseThrow(() -> noEncontrado("Detalle de sesion"));
        sesionPropia(e.getIdSesion());
        return e;
    }
    private void sesionPropia(Integer id) {
        positivo(id, "IdSesion");
        actual.verificar(sesiones.findById(id).orElseThrow(() -> noEncontrado("Sesion")).getIdUsuario());
    }
    private DetalleSesionDTO dto(DetalleSesion e) {
        return new DetalleSesionDTO(e.getIdDetalle(), e.getIdSesion(), e.getIdEjercicio(), e.getObservacion());
    }
}
