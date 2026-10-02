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

    // Relación correcta usando la llave foránea
    @ManyToOne
    @JoinColumn(name = "\"IdUsuario\"", nullable = false)
    private Usuarios usuario;

    @Column(name = "\"FechaInicio\"", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "\"FechaFinEstimada\"")
    private LocalDate fechaFinEstimada;

    @Column(name = "\"FechaFinReal\"")
    private LocalDate fechaReal;


    public Ciclos() {}

    // Constructor actualizado recibiendo el objeto Usuarios completo
    public Ciclos(Usuarios usuario, LocalDate fechaInicio, LocalDate fechaFinEstimada) {
        this.usuario = usuario;
        this.fechaInicio = fechaInicio;
        this.fechaFinEstimada = fechaFinEstimada;
    }

    public Long getIdCiclo() {
        return idCiclo;
    }

    public void setIdCiclo(Long idCiclo) {
        this.idCiclo = idCiclo;
    }

    public Usuarios getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuarios usuario) {
        this.usuario = usuario;
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
