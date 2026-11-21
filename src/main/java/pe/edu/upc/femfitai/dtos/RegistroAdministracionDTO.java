package pe.edu.upc.femfitai.dtos;

/** Registro administrativo sin entidades JPA ni datos de autenticación. */
public record RegistroAdministracionDTO(Integer idUsuario, Object datos, boolean modificable) {
    public RegistroAdministracionDTO(Integer idUsuario, Object datos) { this(idUsuario,datos,true); }
}
