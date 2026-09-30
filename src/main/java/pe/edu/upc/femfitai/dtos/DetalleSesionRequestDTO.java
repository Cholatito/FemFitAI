package pe.edu.upc.femfitai.dtos;

public class DetalleSesionRequestDTO {
    private Integer idSesion;
    private Integer idEjercicio;
    private String observacion;

    public Integer getIdSesion() { return idSesion; }
    public void setIdSesion(Integer idSesion) { this.idSesion = idSesion; }
    public Integer getIdEjercicio() { return idEjercicio; }
    public void setIdEjercicio(Integer idEjercicio) { this.idEjercicio = idEjercicio; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
