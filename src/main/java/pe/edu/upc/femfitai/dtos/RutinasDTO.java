package pe.edu.upc.femfitai.dtos;

import java.time.LocalDateTime;

public class RutinasDTO {
    private Integer idRutina;
    private Integer idUsuario;
    private String nombre;
    private String objetivo;
    private String nivel;
    private LocalDateTime fechaCreacion;
    private Boolean estado;

    public RutinasDTO() {
    }

    public RutinasDTO(Integer idRutina, Integer idUsuario, String nombre, String objetivo, String nivel, LocalDateTime fechaCreacion, Boolean estado) {
        this.idRutina = idRutina;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.objetivo = objetivo;
        this.nivel = nivel;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
    }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }

    public Integer getIdRutina() {
        return idRutina;
    }

    public void setIdRutina(Integer idRutina) {
        this.idRutina = idRutina;
    }
}