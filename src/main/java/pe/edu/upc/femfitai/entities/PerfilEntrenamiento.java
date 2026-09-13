package pe.edu.upc.femfitai.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "\"PerfilEntrenamiento\"", schema = "public",
        uniqueConstraints = @UniqueConstraint(name = "\"UQ_PerfilEntrenamiento_IdUsuario\"", columnNames = "\"IdUsuario\""))
public class PerfilEntrenamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IdPerfil\"", nullable = false)
    private Integer idPerfil;

    @Column(name = "\"IdUsuario\"", nullable = false)
    private Integer idUsuario;

    @Column(name = "\"NivelEntrenamiento\"", nullable = true, length = 50)
    private String nivelEntrenamiento;

    @Column(name = "\"ObjetivoPrincipal\"", nullable = true, length = 100)
    private String objetivoPrincipal;

    @Column(name = "\"DiasDisponibles\"", nullable = true)
    private Integer diasDisponibles;

    @Column(name = "\"TiempoDisponible\"", nullable = true)
    private Integer tiempoDisponible;

    @Column(name = "\"FechaNacimiento\"", nullable = true)
    private LocalDate fechaNacimiento;

    public PerfilEntrenamiento() {}

    public PerfilEntrenamiento(Integer idUsuario, String nivelEntrenamiento, String objetivoPrincipal, Integer diasDisponibles, Integer tiempoDisponible, LocalDate fechaNacimiento) {
        this.idUsuario = idUsuario;
        this.nivelEntrenamiento = nivelEntrenamiento;
        this.objetivoPrincipal = objetivoPrincipal;
        this.diasDisponibles = diasDisponibles;
        this.tiempoDisponible = tiempoDisponible;
        this.fechaNacimiento = fechaNacimiento;
    }

    public Integer getIdPerfil() {
        return idPerfil;
    }

    public void setIdPerfil(Integer idPerfil) {
        this.idPerfil = idPerfil;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNivelEntrenamiento() {
        return nivelEntrenamiento;
    }

    public void setNivelEntrenamiento(String nivelEntrenamiento) {
        this.nivelEntrenamiento = nivelEntrenamiento;
    }

    public String getObjetivoPrincipal() {
        return objetivoPrincipal;
    }

    public void setObjetivoPrincipal(String objetivoPrincipal) {
        this.objetivoPrincipal = objetivoPrincipal;
    }

    public Integer getDiasDisponibles() {
        return diasDisponibles;
    }

    public void setDiasDisponibles(Integer diasDisponibles) {
        this.diasDisponibles = diasDisponibles;
    }

    public Integer getTiempoDisponible() {
        return tiempoDisponible;
    }

    public void setTiempoDisponible(Integer tiempoDisponible) {
        this.tiempoDisponible = tiempoDisponible;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
}