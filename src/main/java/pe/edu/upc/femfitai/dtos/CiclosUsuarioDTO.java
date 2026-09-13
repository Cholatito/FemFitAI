package pe.edu.upc.femfitai.dtos;

import java.time.LocalDate;

public class CiclosUsuarioDTO {
    private final Long idCiclo;
    private final Long idUsuario;
    private final String nombres;
    private final String apellidos;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFinEstimada;
    private final LocalDate fechaFinReal;

    public CiclosUsuarioDTO(Long idCiclo, Long idUsuario, String nombres, String apellidos, LocalDate fechaInicio, LocalDate fechaFinEstimada, LocalDate fechaFinReal) {
        this.idCiclo = idCiclo;
        this.idUsuario = idUsuario;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.fechaInicio = fechaInicio;
        this.fechaFinEstimada = fechaFinEstimada;
        this.fechaFinReal = fechaFinReal;
    }

    public Long getIdCiclo() {
        return idCiclo;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFinEstimada() {
        return fechaFinEstimada;
    }

    public LocalDate getFechaFinReal() {
        return fechaFinReal;
    }
}