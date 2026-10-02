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
        // 1. Buscamos y guardamos el objeto Rutinas completo
        var rutinaObj = rutinas.findById(datos.idRutina())
                .orElseThrow(() -> noEncontrado("Rutina"));

        // 2. Verificamos al usuario
        actual.verificar(rutinaObj.getUsuario().getIdUsuario());

        // 3. Verificamos que haya un generador disponible
        GeneradorRecomendaciones generador = generadores.getIfAvailable();
        if (generador == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Generacion no disponible: falta definir e integrar la regla o proveedor y los datos minimos requeridos");
        }

        // 4. Generamos el resultado
        // 4. Generamos el resultado
        GeneradorRecomendaciones.Resultado resultado;
        try {
            // Pasamos el usuario extrayéndolo directamente del objeto rutina
            resultado = generador.generar(rutinaObj.getUsuario().getIdUsuario(), datos.idRutina());        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }

        // 5. Validamos el resultado
        if (resultado == null || resultado.contenido() == null || resultado.contenido().isBlank()
                || resultado.motivo() == null || resultado.motivo().isBlank()
                || (resultado.tipo() != null && resultado.tipo().length() > 50)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "El generador no devolvio una recomendacion valida con contenido y motivo");
        }

        // 6. Guardamos pasando los objetos completos
        var e = recomendaciones.saveAndFlush(new RecomendacionesIA(
                rutinaObj.getUsuario(),
                rutinaObj,
                LocalDateTime.now(),
                resultado.tipo(),
                resultado.contenido(),
                resultado.motivo(),
                false
        ));

        // 7. Retornamos el DTO (asegúrate de usar el getFecha() correcto que descubriste antes)
        return new RecomendacionesIADTO(
                e.getIdRecomendacion(),
                (e.getUsuario() != null) ? e.getUsuario().getIdUsuario() : null,
                (e.getRutina() != null) ? e.getRutina().getIdRutina() : null,
                e.getFecha(),
                e.getTipo(),
                e.getContenido(),
                e.getMotivo(),
                e.getAceptada()
        );
    }}