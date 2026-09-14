package pe.edu.upc.femfitai.services.interfaces;

import java.util.List;
import pe.edu.upc.femfitai.dtos.EjerciciosDTOInsert;
import pe.edu.upc.femfitai.dtos.EjerciciosDTOList;
import pe.edu.upc.femfitai.dtos.EjerciciosUsuarioDTO;

public interface IEjerciciosService {
    EjerciciosDTOList registrar(EjerciciosDTOInsert datos);
    List<EjerciciosDTOList> listar();
    EjerciciosDTOList buscarPorId(Integer id);
    void eliminar(Integer id);
    List<EjerciciosDTOList> buscarPorGrupoMuscular(String grupoMuscular);
    List<EjerciciosUsuarioDTO> listarPorUsuario(Integer idUsuario);
}
