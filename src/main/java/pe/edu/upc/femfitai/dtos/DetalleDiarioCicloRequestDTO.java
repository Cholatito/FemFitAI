package pe.edu.upc.femfitai.dtos;

import java.time.LocalDate;

public class DetalleDiarioCicloRequestDTO {
    private Long idCiclo;
    private LocalDate fecha;
    private String faseRegistrada;
    private Integer nivelEnergia;
    private String observaciones;



    public Long getIdCiclo() { return idCiclo; }
    public void setIdCiclo(Long idCiclo) { this.idCiclo = idCiclo; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getFaseRegistrada() { return faseRegistrada; }
    public void setFaseRegistrada(String faseRegistrada) { this.faseRegistrada = faseRegistrada; }
    public Integer getNivelEnergia() { return nivelEnergia; }
    public void setNivelEnergia(Integer nivelEnergia) { this.nivelEnergia = nivelEnergia; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}

