package pe.edu.upc.femfitai.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "\"DetalleDiarioCiclo\"", schema = "public")
public class DetalleDiarioCiclo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IdDetalleDiarioCiclo\"", nullable = false)
    private Integer idDetalleDiarioCiclo;

    @Column(name = "\"IdCiclo\"", nullable = false)
    private Integer idCiclo;

    @Column(name = "\"Fecha\"", nullable = false)
    private LocalDate fecha;

    @Column(name = "\"FaseRegistrada\"", nullable = true, length = 50)
    private String faseRegistrada;

    @Column(name = "\"NivelEnergia\"", nullable = true)
    private Integer nivelEnergia;

    @Column(name = "\"Observaciones\"", nullable = true, length = 255)
    private String observaciones;

    public DetalleDiarioCiclo() {}

    public DetalleDiarioCiclo(Integer idCiclo, LocalDate fecha, String faseRegistrada, Integer nivelEnergia, String observaciones) {
        this.idCiclo = idCiclo;
        this.fecha = fecha;
        this.faseRegistrada = faseRegistrada;
        this.nivelEnergia = nivelEnergia;
        this.observaciones = observaciones;
    }

    public Integer getIdDetalleDiarioCiclo() {
        return idDetalleDiarioCiclo;
    }

    public void setIdDetalleDiarioCiclo(Integer idDetalleDiarioCiclo) {
        this.idDetalleDiarioCiclo = idDetalleDiarioCiclo;
    }

    public Integer getIdCiclo() {
        return idCiclo;
    }

    public void setIdCiclo(Integer idCiclo) {
        this.idCiclo = idCiclo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getFaseRegistrada() {
        return faseRegistrada;
    }

    public void setFaseRegistrada(String faseRegistrada) {
        this.faseRegistrada = faseRegistrada;
    }

    public Integer getNivelEnergia() {
        return nivelEnergia;
    }

    public void setNivelEnergia(Integer nivelEnergia) {
        this.nivelEnergia = nivelEnergia;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}