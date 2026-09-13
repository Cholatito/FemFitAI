package pe.edu.upc.femfitai.dtos;

import java.time.LocalDateTime;

public record UsuariosDTOList(Integer idUsuario, String nombres, String apellidos,
                              String correo, String rol, Boolean estado,
                              LocalDateTime fechaRegistro) {
}