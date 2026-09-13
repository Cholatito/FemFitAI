package pe.edu.upc.femfitai.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "\"Rutinas\"", schema = "public")
public class Rutinas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IdRutina\"", nullable = false)
    private Integer idRutina;

    @Column(name = "\"IdUsuario\"", nullable = false)
    private Integer idUsuario;

    @Column(name = "\"Nombre\"", nullable = false, length = 100)
    private String nombre;

    @Column(name = "\"Objetivo\"", nullable = true, length = 100)
    private String objetivo;

    @Column(name = "\"Nivel\"", nullable = true, length = 50)
    private String nivel;

    @Column(name = "\"FechaCreacion\"", nullable = true)
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime fechaCreacion;

    @Column(name = "\"Estado\"", nullable = true)
    @ColumnDefault("true")
    private Boolean estado;

    public Rutinas() {}

    public Rutinas(Integer idUsuario, String nombre, String objetivo, String nivel, LocalDateTime fechaCreacion, Boolean estado) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.objetivo = objetivo;
        this.nivel = nivel;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
}