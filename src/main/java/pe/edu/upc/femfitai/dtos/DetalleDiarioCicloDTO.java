package pe.edu.upc.femfitai.dtos;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de DetalleDiarioCiclo; las referencias mantienen los IDs existentes")
public record DetalleDiarioCicloDTO(Integer idDetalleDiarioCiclo, Integer idCiclo, LocalDate fecha, String faseRegistrada, Integer nivelEnergia, String observaciones) {}
