package pe.edu.upc.femfitai.dtos;

import java.time.LocalDate;

public class CiclosDTO {
    private Long idCiclo;
    private Long idUsuario;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;
    private LocalDate fechaReal;

    public CiclosDTO() {
    }

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

    public void setIdCiclo(Long idCiclo) {
        this.idCiclo = idCiclo;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFinEstimada() {
        return fechaFinEstimada;
    }

    public void setFechaFinEstimada(LocalDate fechaFinEstimada) {
        this.fechaFinEstimada = fechaFinEstimada;
    }

    public LocalDate getFechaReal() {
        return fechaReal;
    }

    public void setFechaReal(LocalDate fechaReal) {
        this.fechaReal = fechaReal;
    }
}
