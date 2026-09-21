package pe.edu.upc.femfitai.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class UsuariosDTO {
    private Integer idUsuario;
    private String nombres;
    private String apellidos;
    private String correo;
    private String passwordHash;
    private String rol;
    private Boolean estado;
    private LocalDateTime fechaRegistro;

    public UsuariosDTO() {
    }

    public UsuariosDTO(Integer idUsuario, String nombres, String apellidos, String correo, String rol, Boolean estado, LocalDateTime fechaRegistro) {
        this.idUsuario = idUsuario;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.rol = rol;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
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

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getPasswordHash() {
        return passwordHash;
    }

    @JsonAlias("password")
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

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
