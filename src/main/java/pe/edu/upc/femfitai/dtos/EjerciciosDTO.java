package pe.edu.upc.femfitai.dtos;

public class EjerciciosDTO {
    private Integer idEjercicio;
    private String nombre;
    private String grupoMuscular;
    private String tipo;
    private String descripcion;

    public EjerciciosDTO() {
    }

    public EjerciciosDTO(Integer idEjercicio, String nombre, String grupoMuscular, String tipo, String descripcion) {
        this.idEjercicio=idEjercicio;
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

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getGrupoMuscular() { return grupoMuscular; }
    public void setGrupoMuscular(String grupoMuscular) { this.grupoMuscular = grupoMuscular; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
