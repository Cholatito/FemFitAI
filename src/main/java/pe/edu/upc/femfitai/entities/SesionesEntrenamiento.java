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

    @ManyToOne
    @JoinColumn(name = "\"IdRutina\"", nullable = false)
    private Rutinas rutina;

    @ManyToOne
    @JoinColumn(name = "\"IdUsuario\"", nullable = false)
    private Usuarios usuario;

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

    public SesionesEntrenamiento(Rutinas rutina, Usuarios usuario, LocalDateTime fecha, Integer duracionMin, Integer nivelEnergia, Integer esfuerzoPercibido, String estado) {
        this.rutina = rutina;
        this.usuario = usuario;
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

    public Rutinas getRutina() {
        return rutina;
    }

    public void setRutina(Rutinas rutina) {
        this.rutina = rutina;
    }

    public Usuarios getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuarios usuario) {
        this.usuario = usuario;
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