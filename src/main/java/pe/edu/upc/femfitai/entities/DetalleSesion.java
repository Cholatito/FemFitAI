package pe.edu.upc.femfitai.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "\"DetalleSesion\"", schema = "public")
public class DetalleSesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IdDetalle\"", nullable = false)
    private Integer idDetalle;

    @Column(name = "\"IdSesion\"", nullable = false)
    private Integer idSesion;

    @Column(name = "\"IdEjercicio\"", nullable = false)
    private Integer idEjercicio;

    @Column(name = "\"Observacion\"", nullable = true, length = 255)
    private String observacion;

    public DetalleSesion() {}

    public DetalleSesion(Integer idSesion, Integer idEjercicio, String observacion) {
        this.idSesion = idSesion;
        this.idEjercicio = idEjercicio;
        this.observacion = observacion;
    }

    public Integer getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Integer idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Integer getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(Integer idSesion) {
        this.idSesion = idSesion;
    }

    public Integer getIdEjercicio() {
        return idEjercicio;
    }

    public void setIdEjercicio(Integer idEjercicio) {
        this.idEjercicio = idEjercicio;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}