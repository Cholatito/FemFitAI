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

        // 1. Buscamos los objetos completos en sus repositorios
        var rutinaObj = rutinas.findById(d.idRutina())
                .orElseThrow(() -> noEncontrado("Rutina"));
        var ejercicioObj = ejercicios.findById(d.idEjercicio()) // Asumo que inyectaste el repositorio de ejercicios
                .orElseThrow(() -> noEncontrado("Ejercicio"));

        // 2. Pasamos los objetos completos al constructor
        return dto(repository.save(new RutinaEjercicios(
                rutinaObj,
                ejercicioObj,
                d.series(),
                d.repeticiones(),
                d.descansoSeg()
        )));
    }

    @Transactional
    public RutinaEjerciciosDTO actualizar(Integer id, RutinaEjerciciosDTO d) {
        positivo(id, "IdRutinaEjercicio");
        var e = repository.findById(id).orElseThrow(() -> noEncontrado("Ejercicio de rutina"));

        // 3. Navegamos por el objeto rutina para validar propiedad
        rutinaPropia(e.getRutina().getIdRutina());
        validar(d);

        // 4. Navegamos por los objetos para comparar los IDs en la validación
        exigir(e.getRutina().getIdRutina().equals(d.idRutina()) &&
                        e.getEjercicio().getIdEjercicio().equals(d.idEjercicio()),
                "No se pueden cambiar las referencias de una rutina o ejercicio");

        e.setSeries(d.series());
        e.setRepeticiones(d.repeticiones());
        e.setDescansoSeg(d.descansoSeg());
        return dto(repository.save(e));
    }

    @Transactional(readOnly = true)
    public List<RutinaEjercicioDetalleDTO> listarPorRutina(Integer idRutina) {
        rutinaPropia(idRutina);
        return repository.listarDetalle(idRutina);
    }

    private void rutinaPropia(Integer id) {
        positivo(id, "IdRutina");
        // 5. Navegamos por el objeto Usuario dentro de la Rutina
        actual.verificar(rutinas.findById(id)
                .orElseThrow(() -> noEncontrado("Rutina"))
                .getUsuario().getIdUsuario());
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
        // Extraemos los IDs de forma segura desde los objetos relacionados
        Integer idRutina = (e.getRutina() != null) ? e.getRutina().getIdRutina() : null;
        Integer idEjercicio = (e.getEjercicio() != null) ? e.getEjercicio().getIdEjercicio() : null;

        return new RutinaEjerciciosDTO(
                e.getIdRutinaEjercicio(),
                idRutina,
                idEjercicio,
                e.getSeries(),
                e.getRepeticiones(),
                e.getDescansoSeg() // Ajusta este último parámetro si tu getter se llama diferente
        );
    }
}
