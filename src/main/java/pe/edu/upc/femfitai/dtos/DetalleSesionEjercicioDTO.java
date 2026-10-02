package pe.edu.upc.femfitai.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de un ejercicio dentro de una sesion, con datos del ejercicio via JOIN")
public record DetalleSesionEjercicioDTO(
        Integer idDetalle,
        Integer idSesion,
        Integer idEjercicio,
        String nombre,
        String grupoMuscular,
        String observacion) {}