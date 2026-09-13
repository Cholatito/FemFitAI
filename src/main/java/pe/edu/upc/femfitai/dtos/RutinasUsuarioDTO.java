package pe.edu.upc.femfitai.dtos;

import java.time.LocalDateTime;

public record RutinasUsuarioDTO(Integer idRutina, Integer idUsuario,
                                String nombres, String apellidos, String nombre,
                                String objetivo, String nivel,
                                LocalDateTime fechaCreacion, Boolean estado) {
}