package pe.edu.upc.femfitai.dtos;
public record RutinaEjercicioDetalleDTO(Integer idRutinaEjercicio, Integer idRutina, Integer idEjercicio,
        String nombre, String grupoMuscular, String tipo, String descripcion,
        Integer series, Integer repeticiones, Integer descansoSeg) {}
