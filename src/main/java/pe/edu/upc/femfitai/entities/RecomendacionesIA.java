package pe.edu.upc.femfitai.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "\"RecomendacionesIA\"", schema = "public")
public class RecomendacionesIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IdRecomendacion\"", nullable = false)
    private Integer idRecomendacion;

    @Column(name = "\"IdUsuario\"", nullable = false)
    private Integer idUsuario;

    @Column(name = "\"IdRutina\"", nullable = true)
    private Integer idRutina;

    @Column(name = "\"Fecha\"", nullable = true)
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime fecha;

    @Column(name = "\"Tipo\"", nullable = true, length = 50)
    private String tipo;

    @Column(name = "\"Contenido\"", nullable = true, columnDefinition = "text")
    private String contenido;

    @Column(name = "\"Motivo\"", nullable = true, columnDefinition = "text")
    private String motivo;

    @Column(name = "\"Aceptada\"", nullable = true)
    @ColumnDefault("false")
    private Boolean aceptada;

    public RecomendacionesIA() {}

    public RecomendacionesIA(Integer idUsuario, Integer idRutina, LocalDateTime fecha, String tipo, String contenido, String motivo, Boolean aceptada) {
        this.idUsuario = idUsuario;
        this.idRutina = idRutina;
        this.fecha = fecha;
        this.tipo = tipo;
        this.contenido = contenido;
        this.motivo = motivo;
        this.aceptada = aceptada;
    }

    public Integer getIdRecomendacion() {
        return idRecomendacion;
    }

    public void setIdRecomendacion(Integer idRecomendacion) {
        this.idRecomendacion = idRecomendacion;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdRutina() {
        return idRutina;
    }

    public void setIdRutina(Integer idRutina) {
        this.idRutina = idRutina;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Boolean getAceptada() {
        return aceptada;
    }

    public void setAceptada(Boolean aceptada) {
        this.aceptada = aceptada;
    }
}