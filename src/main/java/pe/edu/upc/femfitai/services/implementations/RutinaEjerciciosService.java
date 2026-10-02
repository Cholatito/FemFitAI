package pe.edu.upc.femfitai.services.implementations;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.femfitai.dtos.*;
import pe.edu.upc.femfitai.entities.*;
import pe.edu.upc.femfitai.repositories.*;
import pe.edu.upc.femfitai.services.interfaces.IRutinaEjerciciosService;
import static pe.edu.upc.femfitai.services.implementations.Validaciones.*;

@Service
public class RutinaEjerciciosService implements IRutinaEjerciciosService {
    private final RutinaEjerciciosRepository repository;
    private final RutinasRepository rutinas;
    private final EjerciciosRepository ejercicios;
    private final UsuarioActualService actual;
    public RutinaEjerciciosService(RutinaEjerciciosRepository repository, RutinasRepository rutinas, EjerciciosRepository ejercicios, UsuarioActualService actual) {
        this.repository = repository;
        this.rutinas = rutinas;
        this.ejercicios = ejercicios;
        this.actual = actual;
    }
    @Transactional
    public RutinaEjerciciosDTO registrar(RutinaEjerciciosDTO d) {
        validar(d);
        return dto(repository.save(new RutinaEjercicios(d.idRutina(), d.idEjercicio(), d.series(), d.repeticiones(), d.descansoSeg())));
    }
    @Transactional
    public RutinaEjerciciosDTO actualizar(Integer id, RutinaEjerciciosDTO d) {
        positivo(id, "IdRutinaEjercicio");
        var e = repository.findById(id).orElseThrow(() -> noEncontrado("Ejercicio de rutina"));
        rutinaPropia(e.getIdRutina());
        validar(d);
        exigir(e.getIdRutina().equals(d.idRutina()) && e.getIdEjercicio().equals(d.idEjercicio()),
                "No se pueden cambiar las referencias de un ejercicio configurado");
        e.setSeries(d.series()); e.setRepeticiones(d.repeticiones()); e.setDescansoSeg(d.descansoSeg());
        return dto(repository.save(e));
    }
    @Transactional(readOnly = true)
    public List<RutinaEjercicioDetalleDTO> listarPorRutina(Integer idRutina) {
        rutinaPropia(idRutina);
        return repository.listarDetalle(idRutina);
    }
    private void rutinaPropia(Integer id) {
        positivo(id, "IdRutina");
        actual.verificar(rutinas.findById(id).orElseThrow(() -> noEncontrado("Rutina")).getIdUsuario());
    }
    private void validar(RutinaEjerciciosDTO d) {
        exigir(d != null, "Los datos son obligatorios");
        rutinaPropia(d.idRutina());
        positivo(d.idEjercicio(), "IdEjercicio");
        if (!ejercicios.existsById(d.idEjercicio())) throw noEncontrado("Ejercicio");
        if (d.series() != null) positivo(d.series(), "Series");
        if (d.repeticiones() != null) positivo(d.repeticiones(), "Repeticiones");
        exigir(d.descansoSeg() == null || d.descansoSeg() >= 0, "DescansoSeg no puede ser negativo");
    }
    private RutinaEjerciciosDTO dto(RutinaEjercicios e) {
        return new RutinaEjerciciosDTO(e.getIdRutinaEjercicio(), e.getIdRutina(), e.getIdEjercicio(), e.getSeries(), e.getRepeticiones(), e.getDescansoSeg());
    }
}
