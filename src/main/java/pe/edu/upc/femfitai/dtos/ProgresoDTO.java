package pe.edu.upc.femfitai.dtos;

import java.time.LocalDate;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de Progreso; las referencias mantienen los IDs existentes")
public record ProgresoDTO(Integer idProgreso, Integer idUsuario, LocalDate fecha, BigDecimal pesoKg, BigDecimal medidaOpcional, String notaPersonal) {}
