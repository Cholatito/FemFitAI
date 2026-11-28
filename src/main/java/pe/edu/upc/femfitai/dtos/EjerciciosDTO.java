package pe.edu.upc.femfitai.dtos;

public class EjerciciosDTO {
    private Integer idEjercicio;
    private String nombre;
    private String grupoMuscular;
    private String tipo;
    private String descripcion;
    private String imagenUrl;
    private String imagenAutor;
    private String imagenLicencia;
    private String imagenLicenciaUrl;
    private boolean imagenIncluida;

    public EjerciciosDTO() {
    }

    public EjerciciosDTO(Integer idEjercicio, String nombre, String grupoMuscular, String tipo, String descripcion) {
        this.idEjercicio=idEjercicio;
        this.nombre = nombre;
        this.grupoMuscular = grupoMuscular;
        this.tipo = tipo;
        this.descripcion = descripcion;
    }

    // Permite que clientes antiguos actualicen texto sin borrar una imagen existente.
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isImagenIncluida() { return imagenIncluida; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; this.imagenIncluida = true; }
    public String getImagenAutor() { return imagenAutor; }
    public void setImagenAutor(String imagenAutor) { this.imagenAutor = imagenAutor; }
    public String getImagenLicencia() { return imagenLicencia; }
    public void setImagenLicencia(String imagenLicencia) { this.imagenLicencia = imagenLicencia; }
    public String getImagenLicenciaUrl() { return imagenLicenciaUrl; }
    public void setImagenLicenciaUrl(String imagenLicenciaUrl) { this.imagenLicenciaUrl = imagenLicenciaUrl; }

    public Integer getIdEjercicio() {
        return idEjercicio;
    }

    public void setIdEjercicio(Integer idEjercicio) {
        this.idEjercicio = idEjercicio;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getGrupoMuscular() { return grupoMuscular; }
    public void setGrupoMuscular(String grupoMuscular) { this.grupoMuscular = grupoMuscular; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
