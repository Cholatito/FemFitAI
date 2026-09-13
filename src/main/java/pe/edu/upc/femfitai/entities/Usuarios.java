package pe.edu.upc.femfitai.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "\"Usuarios\"", schema = "public",
        uniqueConstraints = @UniqueConstraint(name = "\"UQ_Usuarios_Correo\"", columnNames = "\"Correo\""))
public class Usuarios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IdUsuario\"", nullable = false)
    private Integer idUsuario;

    @Column(name = "\"Nombres\"", nullable = false, length = 100)
    private String nombres;

    @Column(name = "\"Apellidos\"", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "\"Correo\"", nullable = false, length = 150)
    private String correo;

    @Column(name = "\"PasswordHash\"", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "\"Rol\"", nullable = true, length = 30)
    @ColumnDefault("'USUARIA'")
    private String rol;

    @Column(name = "\"Estado\"", nullable = true)
    @ColumnDefault("true")
    private Boolean estado;

    @Column(name = "\"FechaRegistro\"", nullable = true)
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime fechaRegistro;

    public Usuarios() {}

    public Usuarios(String nombres, String apellidos, String correo, String passwordHash, String rol, Boolean estado, LocalDateTime fechaRegistro) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
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

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}