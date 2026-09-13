package pe.edu.upc.femfitai.dtos;

import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDate;

public class CiclosDTOInsert {

    private Long idUsuario;


    private LocalDate fechaInicio;

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
}
