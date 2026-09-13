package pe.edu.upc.femfitai.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;

@Entity
@Table(name = "\"Ciclos\"")
public class Ciclos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IdCiclo\"", nullable = false)
    @JdbcTypeCode(SqlTypes.INTEGER)
    private Long idCiclo;

    @Column(name = "\"IdUsuario\"", nullable = false)
    @JdbcTypeCode(SqlTypes.INTEGER)
    private Long idUsuario;

    @Column(name = "\"FechaInicio\"", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "\"FechaFinEstimada\"")
    private LocalDate fechaFinEstimada;

    @Column(name = "\"FechaFinReal\"")
    private LocalDate fechaReal;


    public Ciclos() {}

    // Constructor con parámetros para facilitar registros
    public Ciclos(Long idUsuario, LocalDate fechaInicio, LocalDate fechaFinEstimada) {
        this.idUsuario = idUsuario;
        this.fechaInicio = fechaInicio;
        this.fechaFinEstimada = fechaFinEstimada;
    }

    public Long getIdCiclo() {
        return idCiclo;
    }

    public void setIdCiclo(Long idCiclo) {
        this.idCiclo = idCiclo;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFinEstimada() {
        return fechaFinEstimada;
    }

    public void setFechaFinEstimada(LocalDate fechaFinEstimada) {
        this.fechaFinEstimada = fechaFinEstimada;
    }

    public LocalDate getFechaReal() {
        return fechaReal;
    }

    public void setFechaReal(LocalDate fechaReal) {
        this.fechaReal = fechaReal;
    }
}
