package pe.edu.upc.femfitai.dtos;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de DetalleSerie; las referencias mantienen los IDs existentes")
public record DetalleSerieDTO(Integer idSerie, Integer idDetalle, Integer numeroSerie, Integer repeticiones, BigDecimal pesoKg) {}
