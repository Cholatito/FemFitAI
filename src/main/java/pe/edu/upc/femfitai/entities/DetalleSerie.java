package pe.edu.upc.femfitai.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "\"DetalleSerie\"", schema = "public")
public class DetalleSerie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IdSerie\"", nullable = false)
    private Integer idSerie;

    @ManyToOne
    @JoinColumn(name = "\"IdDetalle\"", nullable = false)
    private DetalleSesion detalle;

    @Column(name = "\"NumeroSerie\"", nullable = false)
    private Integer numeroSerie;

    @Column(name = "\"Repeticiones\"", nullable = false)
    private Integer repeticiones;

    @Column(name = "\"PesoKg\"", nullable = false, precision = 6, scale = 2)
    private BigDecimal pesoKg;

    public DetalleSerie() {}

    public DetalleSerie(DetalleSesion detalle, Integer numeroSerie, Integer repeticiones, BigDecimal pesoKg) {
        this.detalle = detalle;
        this.numeroSerie = numeroSerie;
        this.repeticiones = repeticiones;
        this.pesoKg = pesoKg;
    }

    public Integer getIdSerie() {
        return idSerie;
    }

    public void setIdSerie(Integer idSerie) {
        this.idSerie = idSerie;
    }

    public DetalleSesion getDetalle() {
        return detalle;
    }

    public void setDetalle(DetalleSesion detalle) {
        this.detalle = detalle;
    }

    public Integer getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(Integer numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public Integer getRepeticiones() {
        return repeticiones;
    }

    public void setRepeticiones(Integer repeticiones) {
        this.repeticiones = repeticiones;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
    }
}