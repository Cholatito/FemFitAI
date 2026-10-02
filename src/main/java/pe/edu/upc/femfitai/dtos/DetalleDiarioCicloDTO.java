package pe.edu.upc.femfitai.dtos;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

public class DetalleDiarioCicloDTO {
    private Integer idDetalleDiarioCiclo;
    private Long idCiclo;
    private LocalDate fecha;
    private String faseRegistrada;
    private Integer nivelEnergia;
    private String observaciones;

    public DetalleDiarioCicloDTO() {
    }

    public DetalleDiarioCicloDTO(Integer idDetalleDiarioCiclo, Long idCiclo, LocalDate fecha,
                                 String faseRegistrada, Integer nivelEnergia, String observaciones) {
        this.idDetalleDiarioCiclo = idDetalleDiarioCiclo;
        this.idCiclo = idCiclo;
        this.fecha = fecha;
        this.faseRegistrada = faseRegistrada;
        this.nivelEnergia = nivelEnergia;
        this.observaciones = observaciones;
    }

    public Integer getIdDetalleDiarioCiclo() { return idDetalleDiarioCiclo; }
    public void setIdDetalleDiarioCiclo(Integer idDetalleDiarioCiclo) { this.idDetalleDiarioCiclo = idDetalleDiarioCiclo; }
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
