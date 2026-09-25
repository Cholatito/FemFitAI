package pe.edu.upc.femfitai.services.implementations;

import java.time.LocalDateTime;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.femfitai.dtos.GenerarRecomendacionDTO;
import pe.edu.upc.femfitai.dtos.RecomendacionesIADTO;
import pe.edu.upc.femfitai.entities.RecomendacionesIA;
import pe.edu.upc.femfitai.repositories.RecomendacionesIARepository;
import pe.edu.upc.femfitai.repositories.RutinasRepository;
import pe.edu.upc.femfitai.services.interfaces.GeneradorRecomendaciones;
import static pe.edu.upc.femfitai.services.implementations.Validaciones.*;

@Service
public class GeneracionRecomendacionesService {
    private final UsuarioActualService actual;
    private final RutinasRepository rutinas;
    private final RecomendacionesIARepository recomendaciones;
    private final ObjectProvider<GeneradorRecomendaciones> generadores;

    public GeneracionRecomendacionesService(UsuarioActualService actual, RutinasRepository rutinas,
            RecomendacionesIARepository recomendaciones, ObjectProvider<GeneradorRecomendaciones> generadores) {
        this.actual = actual;
        this.rutinas = rutinas;
        this.recomendaciones = recomendaciones;
        this.generadores = generadores;
    }

    public RecomendacionesIADTO generar(GenerarRecomendacionDTO datos) {
        exigir(datos != null, "Los datos son obligatorios");
        Integer usuario = actual.id();
        if (datos.idRutina() != null) {
            positivo(datos.idRutina(), "IdRutina");
            actual.verificar(rutinas.findById(datos.idRutina())
                    .orElseThrow(() -> noEncontrado("Rutina")).getIdUsuario());
        }
        GeneradorRecomendaciones generador = generadores.getIfAvailable();
        if (generador == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Generacion no disponible: falta definir e integrar la regla o proveedor y los datos minimos requeridos");
        }
        // No mantener una transaccion de base de datos abierta durante la generacion.
        var resultado = generador.generar(usuario, datos.idRutina());
        if (resultado == null || resultado.contenido() == null || resultado.contenido().isBlank()
                || resultado.motivo() == null || resultado.motivo().isBlank()
                || (resultado.tipo() != null && resultado.tipo().length() > 50)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "El generador no devolvio una recomendacion valida con contenido y motivo");
        }
        var e = recomendaciones.saveAndFlush(new RecomendacionesIA(usuario, datos.idRutina(),
                LocalDateTime.now(), resultado.tipo(), resultado.contenido(), resultado.motivo(), false));
        return new RecomendacionesIADTO(e.getIdRecomendacion(), e.getIdUsuario(), e.getIdRutina(),
                e.getFecha(), e.getTipo(), e.getContenido(), e.getMotivo(), e.getAceptada());
    }
}
