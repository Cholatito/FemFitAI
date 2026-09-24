package pe.edu.upc.femfitai.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de RutinaEjercicios; las referencias mantienen los IDs existentes")
public record RutinaEjerciciosDTO(Integer idRutinaEjercicio, Integer idRutina, Integer idEjercicio, Integer series, Integer repeticiones, Integer descansoSeg) {}
