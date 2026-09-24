package pe.edu.upc.femfitai.services.interfaces;

import java.util.List;

import pe.edu.upc.femfitai.dtos.*;

public interface IRutinaEjerciciosService {
    RutinaEjerciciosDTO registrar(RutinaEjerciciosDTO d);
    RutinaEjerciciosDTO actualizar(Integer id, RutinaEjerciciosDTO d);
    List<RutinaEjercicioDetalleDTO> listarPorRutina(Integer idRutina);
}
