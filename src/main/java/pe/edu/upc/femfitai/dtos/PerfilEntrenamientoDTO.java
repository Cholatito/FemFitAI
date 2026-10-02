package pe.edu.upc.femfitai.dtos;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de PerfilEntrenamiento; las referencias mantienen los IDs existentes")
public record PerfilEntrenamientoDTO(Integer idPerfil, Integer idUsuario, String nivelEntrenamiento, String objetivoPrincipal, Integer diasDisponibles, Integer tiempoDisponible, LocalDate fechaNacimiento) {}
