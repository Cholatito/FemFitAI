package pe.edu.upc.femfitai.dtos;

public class RutinaEjerciciosRequestDTO {
    private Integer idRutina;
    private Integer idEjercicio;
    private Integer series;
    private Integer repeticiones;
    private Integer descansoSeg;

    public Integer getIdRutina() { return idRutina; }
    public void setIdRutina(Integer idRutina) { this.idRutina = idRutina; }
    public Integer getIdEjercicio() { return idEjercicio; }
    public void setIdEjercicio(Integer idEjercicio) { this.idEjercicio = idEjercicio; }
    public Integer getSeries() { return series; }
    public void setSeries(Integer series) { this.series = series; }
    public Integer getRepeticiones() { return repeticiones; }
    public void setRepeticiones(Integer repeticiones) { this.repeticiones = repeticiones; }
    public Integer getDescansoSeg() { return descansoSeg; }
    public void setDescansoSeg(Integer descansoSeg) { this.descansoSeg = descansoSeg; }
}
