package pe.edu.upc.femfitai.dtos;

public class FinalizarSesionDTO {
    private Integer duracionMin;
    private Integer nivelEnergia;
    private Integer esfuerzoPercibido;

    public FinalizarSesionDTO() {
    }

    public FinalizarSesionDTO(Integer duracionMin, Integer nivelEnergia, Integer esfuerzoPercibido) {
        this.duracionMin = duracionMin;
        this.nivelEnergia = nivelEnergia;
        this.esfuerzoPercibido = esfuerzoPercibido;
    }

    public Integer getDuracionMin() {
        return duracionMin;
    }

    public void setDuracionMin(Integer duracionMin) {
        this.duracionMin = duracionMin;
    }

    public Integer getNivelEnergia() {
        return nivelEnergia;
    }

    public void setNivelEnergia(Integer nivelEnergia) {
        this.nivelEnergia = nivelEnergia;
    }

    public Integer getEsfuerzoPercibido() {
        return esfuerzoPercibido;
    }

    public void setEsfuerzoPercibido(Integer esfuerzoPercibido) {
        this.esfuerzoPercibido = esfuerzoPercibido;
    }
}
