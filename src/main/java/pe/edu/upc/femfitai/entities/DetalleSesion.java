package pe.edu.upc.femfitai.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "\"DetalleSesion\"", schema = "public")
public class DetalleSesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IdDetalle\"", nullable = false)
    private Integer idDetalle;

    @ManyToOne
    @JoinColumn(name = "\"IdSesion\"", nullable = false)
    private SesionesEntrenamiento sesion;

    @ManyToOne
    @JoinColumn(name = "\"IdEjercicio\"", nullable = false)
    private Ejercicios ejercicio;

    @Column(name = "\"Observacion\"", nullable = true, length = 255)
    private String observacion;

    public DetalleSesion() {}

    public DetalleSesion(SesionesEntrenamiento sesion, Ejercicios ejercicio, String observacion) {
        this.sesion = sesion;
        this.ejercicio = ejercicio;
        this.observacion = observacion;
    }

    public Integer getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Integer idDetalle) {
        this.idDetalle = idDetalle;
    }

    public SesionesEntrenamiento getSesion() {
        return sesion;
    }

    public void setSesion(SesionesEntrenamiento sesion) {
        this.sesion = sesion;
    }

    public Ejercicios getEjercicio() {
        return ejercicio;
    }

    public void setEjercicio(Ejercicios ejercicio) {
        this.ejercicio = ejercicio;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}