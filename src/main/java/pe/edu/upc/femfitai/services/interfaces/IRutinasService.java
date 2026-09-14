package pe.edu.upc.femfitai.services.interfaces;

import java.util.List;
import pe.edu.upc.femfitai.dtos.RutinasDTOInsert;
import pe.edu.upc.femfitai.dtos.RutinasDTOUpdate;
import pe.edu.upc.femfitai.dtos.RutinasDTOList;
import pe.edu.upc.femfitai.dtos.RutinasUsuarioDTO;

public interface IRutinasService {
    RutinasDTOList registrar(RutinasDTOInsert datos);
    List<RutinasDTOList> listar();
    RutinasDTOList buscarPorId(Integer id);
    RutinasDTOList actualizar(Integer id, RutinasDTOUpdate datos);
    void eliminar(Integer id);
    List<RutinasDTOList> listarPorUsuario(Integer idUsuario);
    RutinasUsuarioDTO buscarDetallePorId(Integer id);
}