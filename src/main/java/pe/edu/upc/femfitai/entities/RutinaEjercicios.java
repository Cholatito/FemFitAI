package pe.edu.upc.femfitai.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "\"RutinaEjercicios\"", schema = "public")
public class RutinaEjercicios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IdRutinaEjercicio\"", nullable = false)
    private Integer idRutinaEjercicio;

    @ManyToOne
    @JoinColumn(name = "\"IdRutina\"", nullable = false)
    private Rutinas rutina;

    @ManyToOne
    @JoinColumn(name = "\"IdEjercicio\"", nullable = false)
    private Ejercicios ejercicio;

    @Column(name = "\"Series\"", nullable = true)
    private Integer series;

    @Column(name = "\"Repeticiones\"", nullable = true)
    private Integer repeticiones;

    @Column(name = "\"DescansoSeg\"", nullable = true)
    private Integer descansoSeg;

    public RutinaEjercicios() {}

    public RutinaEjercicios(Rutinas rutina, Ejercicios ejercicio, Integer series, Integer repeticiones, Integer descansoSeg) {
        this.rutina = rutina;
        this.ejercicio = ejercicio;
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

    public Rutinas getRutina() {
        return rutina;
    }

    public void setRutina(Rutinas rutina) {
        this.rutina = rutina;
    }

    public Ejercicios getEjercicio() {
        return ejercicio;
    }

    public void setEjercicio(Ejercicios ejercicio) {
        this.ejercicio = ejercicio;
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