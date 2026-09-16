package pe.edu.upc.femfitai.dtos;

import java.time.LocalDateTime;

public class UsuariosDTO {
    private Integer idUsuario;
    private String nombres;
    private String apellidos;
    private String correo;
    private String passwordHash;
    private String rol;
    private Boolean estado;

    public UsuariosDTO(Integer idUsuario, String nombres, String apellidos, String correo, String rol, Boolean estado, LocalDateTime fechaRegistro) {
    }

    public UsuariosDTO(Integer idUsuario, String nombres, String apellidos, String correo, String passwordHash, String rol, Boolean estado) {
        this.idUsuario = idUsuario;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.estado = estado;
    }


    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
}