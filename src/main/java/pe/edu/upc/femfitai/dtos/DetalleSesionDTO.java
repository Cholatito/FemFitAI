package pe.edu.upc.femfitai.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de DetalleSesion; las referencias mantienen los IDs existentes")
public record DetalleSesionDTO(Integer idDetalle, Integer idSesion, Integer idEjercicio, String observacion) {}
