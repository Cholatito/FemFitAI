package pe.edu.upc.femfitai.services.implementations;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.femfitai.dtos.DetalleDiarioCicloDTO;
import pe.edu.upc.femfitai.entities.Ciclos;
import pe.edu.upc.femfitai.entities.DetalleDiarioCiclo;
import pe.edu.upc.femfitai.repositories.CiclosRepository;
import pe.edu.upc.femfitai.repositories.DetalleDiarioCicloRepository;
import pe.edu.upc.femfitai.services.interfaces.IDetalleDiarioCicloService;
import static pe.edu.upc.femfitai.services.implementations.Validaciones.*;

@Service
public class DetalleDiarioCicloService implements IDetalleDiarioCicloService {
    // Trello exige una escala establecida, pero no indica sus extremos.
    // Valores provisionales para que el rango quede centralizado y sea facil ajustarlo.
    private static final int ENERGIA_MIN = 1;
    private static final int ENERGIA_MAX = 5;

    private final DetalleDiarioCicloRepository repository;
    private final CiclosRepository ciclos;
    private final UsuarioActualService actual;

    public DetalleDiarioCicloService(DetalleDiarioCicloRepository repository,
                                     CiclosRepository ciclos,
                                     UsuarioActualService actual) {
        this.repository = repository;
        this.ciclos = ciclos;
        this.actual = actual;
    }

    @Override
    @Transactional
    public DetalleDiarioCicloDTO guardar(DetalleDiarioCicloDTO d) {
        exigir(d != null, "Los datos son obligatorios");
        positivo(d.idCiclo(), "IdCiclo");
        Ciclos ciclo = ciclos.findById(d.idCiclo().longValue())
                .orElseThrow(() -> noEncontrado("Ciclo"));
        actual.verificar(ciclo.getIdUsuario());

        LocalDate fecha = d.fecha() == null ? LocalDate.now() : d.fecha();
        exigir(!fecha.isAfter(LocalDate.now()), "Fecha no puede ser futura");
        exigir(d.nivelEnergia() != null, "NivelEnergia es obligatorio");
        exigir(d.nivelEnergia() >= ENERGIA_MIN && d.nivelEnergia() <= ENERGIA_MAX,
                "NivelEnergia debe estar entre " + ENERGIA_MIN + " y " + ENERGIA_MAX);
        texto(d.faseRegistrada(), 50, "FaseRegistrada", false);
        texto(d.observaciones(), 255, "Observaciones", false);

        // US10: upsert por usuaria + fecha. Si existe, se actualiza y no se duplica.
        DetalleDiarioCiclo e = repository.buscarPorUsuarioYFecha(actual.id().longValue(), fecha)
                .stream().findFirst().orElseGet(DetalleDiarioCiclo::new);
        e.setIdCiclo(d.idCiclo());
        e.setFecha(fecha);
        e.setFaseRegistrada(d.faseRegistrada());
        e.setNivelEnergia(d.nivelEnergia());
        e.setObservaciones(d.observaciones());
        return dto(repository.save(e));
    }

    private DetalleDiarioCicloDTO dto(DetalleDiarioCiclo e) {
        return new DetalleDiarioCicloDTO(e.getIdDetalleDiarioCiclo(), e.getIdCiclo(), e.getFecha(),
                e.getFaseRegistrada(), e.getNivelEnergia(), e.getObservaciones());
    }
}
