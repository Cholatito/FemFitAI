package pe.edu.upc.femfitai.services.interfaces;

import pe.edu.upc.femfitai.dtos.DetalleDiarioCicloDTO;
import pe.edu.upc.femfitai.dtos.DetalleDiarioCicloRequestDTO;

import java.util.List;

public interface IDetalleDiarioCicloService {
    DetalleDiarioCicloDTO registrar(DetalleDiarioCicloDTO datos);

    List<DetalleDiarioCicloDTO> listar();

    DetalleDiarioCicloDTO buscarPorId(Integer id);

    DetalleDiarioCicloDTO actualizar(Integer id, DetalleDiarioCicloDTO datos);

    void eliminar(Integer id);

    List<DetalleDiarioCicloDTO> listarPorCiclo(Long idCiclo);
}


