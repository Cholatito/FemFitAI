package pe.edu.upc.femfitai.dtos;

import java.time.LocalDateTime;

public record RecomendacionesIADTO(Integer idRecomendacion, Integer idUsuario, Integer idRutina,
        LocalDateTime fecha, String tipo, String contenido, String motivo, Boolean aceptada) {}
