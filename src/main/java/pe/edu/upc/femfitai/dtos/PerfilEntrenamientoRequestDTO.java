package pe.edu.upc.femfitai.dtos;

import java.time.LocalDate;

public class PerfilEntrenamientoRequestDTO {
    private Integer idUsuario;
    private String nivelEntrenamiento;
    private String objetivoPrincipal;
    private Integer diasDisponibles;
    private Integer tiempoDisponible;
    private LocalDate fechaNacimiento;

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getNivelEntrenamiento() { return nivelEntrenamiento; }
    public void setNivelEntrenamiento(String nivelEntrenamiento) { this.nivelEntrenamiento = nivelEntrenamiento; }
    public String getObjetivoPrincipal() { return objetivoPrincipal; }
    public void setObjetivoPrincipal(String objetivoPrincipal) { this.objetivoPrincipal = objetivoPrincipal; }
    public Integer getDiasDisponibles() { return diasDisponibles; }
    public void setDiasDisponibles(Integer diasDisponibles) { this.diasDisponibles = diasDisponibles; }
    public Integer getTiempoDisponible() { return tiempoDisponible; }
    public void setTiempoDisponible(Integer tiempoDisponible) { this.tiempoDisponible = tiempoDisponible; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
}
