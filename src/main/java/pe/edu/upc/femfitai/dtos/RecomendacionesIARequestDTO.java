package pe.edu.upc.femfitai.dtos;

import java.time.LocalDateTime;

public class RecomendacionesIARequestDTO {
    private Integer idUsuario;
    private Integer idRutina;
    private LocalDateTime fecha;
    private String tipo;
    private String contenido;
    private String motivo;
    private Boolean aceptada;

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public Integer getIdRutina() { return idRutina; }
    public void setIdRutina(Integer idRutina) { this.idRutina = idRutina; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Boolean getAceptada() { return aceptada; }
    public void setAceptada(Boolean aceptada) { this.aceptada = aceptada; }
}
