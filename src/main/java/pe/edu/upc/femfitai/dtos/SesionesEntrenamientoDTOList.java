package pe.edu.upc.femfitai.dtos;

import java.time.LocalDateTime;

public record SesionesEntrenamientoDTOList(
        Integer idSesion, Integer idRutina, Integer idUsuario, LocalDateTime fecha,
        Integer duracionMin, Integer nivelEnergia, Integer esfuerzoPercibido, String estado) {
}
