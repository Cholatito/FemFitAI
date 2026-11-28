package pe.edu.upc.femfitai.dtos;

import java.time.LocalDateTime;

public record UsuarioDetalleDTO(
        Integer idUsuario,
        String nombres,
        String apellidos,
        String correo,
        String rol,
        Boolean estado,
        LocalDateTime fechaRegistro,
        PerfilEntrenamientoDTO perfil,   // null si aún no creó su perfil
        long totalRutinas,
        long totalSesiones,
        long totalProgreso,
        long totalCiclos) {}