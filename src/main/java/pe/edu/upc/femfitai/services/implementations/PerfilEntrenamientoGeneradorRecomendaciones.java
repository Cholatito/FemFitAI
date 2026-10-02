package pe.edu.upc.femfitai.services.implementations;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import pe.edu.upc.femfitai.entities.PerfilEntrenamiento;
import pe.edu.upc.femfitai.repositories.PerfilEntrenamientoRepository;
import pe.edu.upc.femfitai.services.interfaces.GeneradorRecomendaciones;

@Component
public class PerfilEntrenamientoGeneradorRecomendaciones implements GeneradorRecomendaciones {
    private final PerfilEntrenamientoRepository perfilEntrenamientoRepository;

    public PerfilEntrenamientoGeneradorRecomendaciones(PerfilEntrenamientoRepository perfilEntrenamientoRepository) {
        this.perfilEntrenamientoRepository = perfilEntrenamientoRepository;
    }

    @Override
    public Resultado generar(Integer idUsuario, Integer idRutina) {
        PerfilEntrenamiento perfil = perfilEntrenamientoRepository.findByIdUsuario(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Completa tu perfil de entrenamiento antes de generar una recomendación"));

        List<String> faltantes = new ArrayList<>();
        if (perfil.getNivelEntrenamiento() == null || perfil.getNivelEntrenamiento().isBlank()) {
            faltantes.add("NivelEntrenamiento");
        }
        if (perfil.getObjetivoPrincipal() == null || perfil.getObjetivoPrincipal().isBlank()) {
            faltantes.add("ObjetivoPrincipal");
        }
        if (perfil.getDiasDisponibles() == null || perfil.getDiasDisponibles() < 1 || perfil.getDiasDisponibles() > 7) {
            faltantes.add("DiasDisponibles");
        }
        if (perfil.getTiempoDisponible() == null || perfil.getTiempoDisponible() <= 0) {
            faltantes.add("TiempoDisponible");
        }

        if (!faltantes.isEmpty()) {
            throw new IllegalArgumentException("Faltan datos del perfil: " + String.join(", ", faltantes));
        }

        String nivel = perfil.getNivelEntrenamiento().trim();
        String objetivo = perfil.getObjetivoPrincipal().trim();
        Integer diasDisponibles = perfil.getDiasDisponibles();
        Integer tiempoDisponible = perfil.getTiempoDisponible();

        String contenido = String.format(
                "Plan recomendado para %s: %d sesiones por semana de aproximadamente %d minutos, enfocadas en %s y con progresión gradual.",
                nivel,
                diasDisponibles,
                tiempoDisponible,
                objetivo.toLowerCase()
        );

        String motivo = String.format(
                "Se recomienda esta distribución porque la usuaria indicó nivel %s, objetivo %s, %d días disponibles y %d minutos por sesión.",
                nivel,
                objetivo,
                diasDisponibles,
                tiempoDisponible
        );

        return new Resultado("PerfilEntrenamiento", contenido, motivo);
    }
}
