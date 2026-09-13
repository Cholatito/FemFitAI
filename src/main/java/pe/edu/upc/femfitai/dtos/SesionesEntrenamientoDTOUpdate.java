package pe.edu.upc.femfitai.dtos;

import java.time.LocalDateTime;

public class SesionesEntrenamientoDTOUpdate {
    private Integer idRutina;
    private Integer idUsuario;
    private LocalDateTime fecha;
    private Integer duracionMin;
    private Integer nivelEnergia;
    private Integer esfuerzoPercibido;
    private String estado;

    public Integer getIdRutina() { return idRutina; }
    public void setIdRutina(Integer idRutina) { this.idRutina = idRutina; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Integer getDuracionMin() { return duracionMin; }
    public void setDuracionMin(Integer duracionMin) { this.duracionMin = duracionMin; }

    public Integer getNivelEnergia() { return nivelEnergia; }
    public void setNivelEnergia(Integer nivelEnergia) { this.nivelEnergia = nivelEnergia; }

    public Integer getEsfuerzoPercibido() { return esfuerzoPercibido; }
    public void setEsfuerzoPercibido(Integer esfuerzoPercibido) { this.esfuerzoPercibido = esfuerzoPercibido; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
