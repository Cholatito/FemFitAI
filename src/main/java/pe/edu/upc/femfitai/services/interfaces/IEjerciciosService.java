package pe.edu.upc.femfitai.services.interfaces;

import java.util.List;
import pe.edu.upc.femfitai.dtos.EjerciciosDTO;
import pe.edu.upc.femfitai.dtos.EjerciciosUsuarioDTO;

public interface IEjerciciosService {
    EjerciciosDTO registrar(EjerciciosDTO datos);
    List<EjerciciosDTO> listar();
    EjerciciosDTO buscarPorId(Integer id);
    EjerciciosDTO actualizar(Integer id, EjerciciosDTO datos);
    void eliminar(Integer id);
    List<EjerciciosDTO> buscarPorGrupoMuscular(String grupoMuscular);
    List<EjerciciosUsuarioDTO> listarPorUsuario(Integer idUsuario);
}
