package pe.edu.upc.femfitai.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "\"Ejercicios\"", schema = "public")
public class Ejercicios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IdEjercicio\"", nullable = false)
    private Integer idEjercicio;
    @Column(name = "\"WgerId\"")
    private Integer wgerId;
    @Column(name = "\"Nombre\"", nullable = false, length = 100)
    private String nombre;

    @Column(name = "\"GrupoMuscular\"", nullable = true, length = 100)
    private String grupoMuscular;

    @Column(name = "\"Tipo\"", nullable = true, length = 50)
    private String tipo;

    @Column(name = "\"Descripcion\"", nullable = true, columnDefinition = "text")
    private String descripcion;

    @Column(name = "\"ImagenUrl\"", length = 2048)
    private String imagenUrl;

    @Column(name = "\"ImagenAutor\"", length = 255)
    private String imagenAutor;

    @Column(name = "\"ImagenLicencia\"", length = 255)
    private String imagenLicencia;

    @Column(name = "\"ImagenLicenciaUrl\"", length = 2048)
    private String imagenLicenciaUrl;

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


    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public String getImagenAutor() { return imagenAutor; }
    public void setImagenAutor(String imagenAutor) { this.imagenAutor = imagenAutor; }
    public String getImagenLicencia() { return imagenLicencia; }
    public void setImagenLicencia(String imagenLicencia) { this.imagenLicencia = imagenLicencia; }
    public String getImagenLicenciaUrl() { return imagenLicenciaUrl; }
    public void setImagenLicenciaUrl(String imagenLicenciaUrl) { this.imagenLicenciaUrl = imagenLicenciaUrl; }

    public Integer getWgerId() {
        return wgerId;
    }

    public void setWgerId(Integer wgerId) {
        this.wgerId = wgerId;
    }
}