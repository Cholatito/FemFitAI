package pe.edu.upc.femfitai.services.interfaces;

import java.util.List;
import pe.edu.upc.femfitai.dtos.RutinasDTO;
import pe.edu.upc.femfitai.dtos.RutinasUsuarioDTO;

public interface IRutinasService {
    RutinasDTO registrar(RutinasDTO datos);
    List<RutinasDTO> listar();
    RutinasDTO buscarPorId(Integer id);
    RutinasDTO actualizar(Integer id, RutinasDTO datos);
    void eliminar(Integer id);
    List<RutinasDTO> listarPorUsuario(Integer idUsuario);
    RutinasUsuarioDTO buscarDetallePorId(Integer id);
}