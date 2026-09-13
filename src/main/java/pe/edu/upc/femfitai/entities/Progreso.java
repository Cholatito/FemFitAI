package pe.edu.upc.femfitai.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "\"Progreso\"", schema = "public")
public class Progreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IdProgreso\"", nullable = false)
    private Integer idProgreso;

    @Column(name = "\"IdUsuario\"", nullable = false)
    private Integer idUsuario;

    @Column(name = "\"Fecha\"", nullable = true)
    @ColumnDefault("CURRENT_DATE")
    private LocalDate fecha;

    @Column(name = "\"PesoKg\"", nullable = true, precision = 6, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "\"MedidaOpcional\"", nullable = true, precision = 6, scale = 2)
    private BigDecimal medidaOpcional;

    @Column(name = "\"NotaPersonal\"", nullable = true, length = 255)
    private String notaPersonal;

    public Progreso() {}

    public Progreso(Integer idUsuario, LocalDate fecha, BigDecimal pesoKg, BigDecimal medidaOpcional, String notaPersonal) {
        this.idUsuario = idUsuario;
        this.fecha = fecha;
        this.pesoKg = pesoKg;
        this.medidaOpcional = medidaOpcional;
        this.notaPersonal = notaPersonal;
    }

    public Integer getIdProgreso() {
        return idProgreso;
    }

    public void setIdProgreso(Integer idProgreso) {
        this.idProgreso = idProgreso;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
    }

    public BigDecimal getMedidaOpcional() {
        return medidaOpcional;
    }

    public void setMedidaOpcional(BigDecimal medidaOpcional) {
        this.medidaOpcional = medidaOpcional;
    }

    public String getNotaPersonal() {
        return notaPersonal;
    }

    public void setNotaPersonal(String notaPersonal) {
        this.notaPersonal = notaPersonal;
    }
}