package pe.edu.upc.femfitai.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "\"SesionesEntrenamiento\"", schema = "public")
public class SesionesEntrenamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IdSesion\"", nullable = false)
    private Integer idSesion;

    @Column(name = "\"IdRutina\"", nullable = false)
    private Integer idRutina;

    @Column(name = "\"IdUsuario\"", nullable = false)
    private Integer idUsuario;

    @Column(name = "\"Fecha\"", nullable = true)
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime fecha;

    @Column(name = "\"DuracionMin\"", nullable = true)
    private Integer duracionMin;

    @Column(name = "\"NivelEnergia\"", nullable = true)
    private Integer nivelEnergia;

    @Column(name = "\"EsfuerzoPercibido\"", nullable = true)
    private Integer esfuerzoPercibido;

    @Column(name = "\"Estado\"", nullable = true, length = 30)
    private String estado;

    public SesionesEntrenamiento() {}

    public SesionesEntrenamiento(Integer idRutina, Integer idUsuario, LocalDateTime fecha, Integer duracionMin, Integer nivelEnergia, Integer esfuerzoPercibido, String estado) {
        this.idRutina = idRutina;
        this.idUsuario = idUsuario;
        this.fecha = fecha;
        this.duracionMin = duracionMin;
        this.nivelEnergia = nivelEnergia;
        this.esfuerzoPercibido = esfuerzoPercibido;
        this.estado = estado;
    }

    public Integer getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(Integer idSesion) {
        this.idSesion = idSesion;
    }

    public Integer getIdRutina() {
        return idRutina;
    }

    public void setIdRutina(Integer idRutina) {
        this.idRutina = idRutina;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Integer getDuracionMin() {
        return duracionMin;
    }

    public void setDuracionMin(Integer duracionMin) {
        this.duracionMin = duracionMin;
    }

    public Integer getNivelEnergia() {
        return nivelEnergia;
    }

    public void setNivelEnergia(Integer nivelEnergia) {
        this.nivelEnergia = nivelEnergia;
    }

    public Integer getEsfuerzoPercibido() {
        return esfuerzoPercibido;
    }

    public void setEsfuerzoPercibido(Integer esfuerzoPercibido) {
        this.esfuerzoPercibido = esfuerzoPercibido;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}