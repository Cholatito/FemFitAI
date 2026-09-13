package pe.edu.upc.femfitai.dtos;

import java.time.LocalDateTime;

public record SesionesEntrenamientoDTODetalle(
        Integer idSesion, Integer idUsuario, String nombres, String apellidos,
        Integer idRutina, String nombreRutina, LocalDateTime fecha,
        Integer duracionMin, Integer nivelEnergia, Integer esfuerzoPercibido, String estado) {
}
