package pe.edu.upc.femfitai.services.interfaces;

import java.util.List;

import pe.edu.upc.femfitai.dtos.*;

public interface IDetalleSesionService {
    DetalleSesionDTO registrar(DetalleSesionDTO d);
    List<DetalleSesionEjercicioDTO> listarPorSesion(Integer idSesion);
    DetalleSesionDTO actualizar(Integer id, ObservacionSesionDTO d);
    void eliminar(Integer id);
}
