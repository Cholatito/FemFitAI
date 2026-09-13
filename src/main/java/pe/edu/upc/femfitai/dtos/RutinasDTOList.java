package pe.edu.upc.femfitai.dtos;

import java.time.LocalDateTime;

public record RutinasDTOList(Integer idRutina, Integer idUsuario, String nombre,
                             String objetivo, String nivel, LocalDateTime fechaCreacion,
                             Boolean estado) {
}