package pe.edu.upc.femfitai.dtos;

public record EjerciciosUsuarioDTO(Integer idUsuario, String nombres, String apellidos,
                                   Integer idRutina, String nombreRutina, Integer idRutinaEjercicio,
                                   Integer idEjercicio, String nombreEjercicio, String grupoMuscular,
                                   String tipo, Integer series, Integer repeticiones, Integer descansoSeg) {
}
