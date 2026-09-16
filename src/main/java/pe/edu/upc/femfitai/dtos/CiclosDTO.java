package pe.edu.upc.femfitai.dtos;

import java.time.LocalDate;

public class CiclosDTO {
    private Long idCiclo;
    private Long idUsuario;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;
    private LocalDate fechaReal;

    public CiclosDTO(Long idCiclo, Long idUsuario, LocalDate fechaInicio, LocalDate fechaFinEstimada, LocalDate fechaReal) {
        this.idCiclo = idCiclo;
        this.idUsuario = idUsuario;
        this.fechaInicio = fechaInicio;
        this.fechaFinEstimada = fechaFinEstimada;
        this.fechaReal = fechaReal;
    }

    public Long getIdCiclo() {
        return idCiclo;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFinEstimada() {
        return fechaFinEstimada;
    }

    public LocalDate getFechaReal() {
        return fechaReal;
    }
}
