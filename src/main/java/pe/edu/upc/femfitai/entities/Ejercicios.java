package pe.edu.upc.femfitai.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "\"Ejercicios\"", schema = "public")
public class Ejercicios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IdEjercicio\"", nullable = false)
    private Integer idEjercicio;

    @Column(name = "\"Nombre\"", nullable = false, length = 100)
    private String nombre;

    @Column(name = "\"GrupoMuscular\"", nullable = true, length = 100)
    private String grupoMuscular;

    @Column(name = "\"Tipo\"", nullable = true, length = 50)
    private String tipo;

    @Column(name = "\"Descripcion\"", nullable = true, columnDefinition = "text")
    private String descripcion;

    public Ejercicios() {}

    public Ejercicios(String nombre, String grupoMuscular, String tipo, String descripcion) {
        this.nombre = nombre;
        this.grupoMuscular = grupoMuscular;
        this.tipo = tipo;
        this.descripcion = descripcion;
    }

    public Integer getIdEjercicio() {
        return idEjercicio;
    }

    public void setIdEjercicio(Integer idEjercicio) {
        this.idEjercicio = idEjercicio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getGrupoMuscular() {
        return grupoMuscular;
    }

    public void setGrupoMuscular(String grupoMuscular) {
        this.grupoMuscular = grupoMuscular;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}