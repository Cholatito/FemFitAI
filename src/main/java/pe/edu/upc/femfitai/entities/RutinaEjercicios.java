package pe.edu.upc.femfitai.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "\"RutinaEjercicios\"", schema = "public")
public class RutinaEjercicios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IdRutinaEjercicio\"", nullable = false)
    private Integer idRutinaEjercicio;

    @Column(name = "\"IdRutina\"", nullable = false)
    private Integer idRutina;

    @Column(name = "\"IdEjercicio\"", nullable = false)
    private Integer idEjercicio;

    @Column(name = "\"Series\"", nullable = true)
    private Integer series;

    @Column(name = "\"Repeticiones\"", nullable = true)
    private Integer repeticiones;

    @Column(name = "\"DescansoSeg\"", nullable = true)
    private Integer descansoSeg;

    public RutinaEjercicios() {}

    public RutinaEjercicios(Integer idRutina, Integer idEjercicio, Integer series, Integer repeticiones, Integer descansoSeg) {
        this.idRutina = idRutina;
        this.idEjercicio = idEjercicio;
        this.series = series;
        this.repeticiones = repeticiones;
        this.descansoSeg = descansoSeg;
    }

    public Integer getIdRutinaEjercicio() {
        return idRutinaEjercicio;
    }

    public void setIdRutinaEjercicio(Integer idRutinaEjercicio) {
        this.idRutinaEjercicio = idRutinaEjercicio;
    }

    public Integer getIdRutina() {
        return idRutina;
    }

    public void setIdRutina(Integer idRutina) {
        this.idRutina = idRutina;
    }

    public Integer getIdEjercicio() {
        return idEjercicio;
    }

    public void setIdEjercicio(Integer idEjercicio) {
        this.idEjercicio = idEjercicio;
    }

    public Integer getSeries() {
        return series;
    }

    public void setSeries(Integer series) {
        this.series = series;
    }

    public Integer getRepeticiones() {
        return repeticiones;
    }

    public void setRepeticiones(Integer repeticiones) {
        this.repeticiones = repeticiones;
    }

    public Integer getDescansoSeg() {
        return descansoSeg;
    }

    public void setDescansoSeg(Integer descansoSeg) {
        this.descansoSeg = descansoSeg;
    }
}