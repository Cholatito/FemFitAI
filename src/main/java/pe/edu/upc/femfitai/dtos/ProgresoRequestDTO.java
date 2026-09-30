package pe.edu.upc.femfitai.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProgresoRequestDTO {
    private Integer idUsuario;
    private LocalDate fecha;
    private BigDecimal pesoKg;
    private BigDecimal medidaOpcional;
    private String notaPersonal;

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public BigDecimal getPesoKg() { return pesoKg; }
    public void setPesoKg(BigDecimal pesoKg) { this.pesoKg = pesoKg; }
    public BigDecimal getMedidaOpcional() { return medidaOpcional; }
    public void setMedidaOpcional(BigDecimal medidaOpcional) { this.medidaOpcional = medidaOpcional; }
    public String getNotaPersonal() { return notaPersonal; }
    public void setNotaPersonal(String notaPersonal) { this.notaPersonal = notaPersonal; }
}
