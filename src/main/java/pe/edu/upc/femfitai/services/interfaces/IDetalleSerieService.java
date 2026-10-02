package pe.edu.upc.femfitai.services.interfaces;

import java.util.List;

import pe.edu.upc.femfitai.dtos.*;

public interface IDetalleSerieService {
    DetalleSerieDTO registrar(DetalleSerieDTO d);
    List<DetalleSerieDTO> listarPorDetalle(Integer idDetalle);
    DetalleSerieDTO actualizar(Integer id, ActualizarSerieDTO d);
    void eliminar(Integer id);
}
