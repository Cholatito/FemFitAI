package pe.edu.upc.femfitai.services.interfaces;

import pe.edu.upc.femfitai.dtos.CiclosUsuarioDTO;

import java.util.List;
import pe.edu.upc.femfitai.dtos.CiclosDTOInsert;
import pe.edu.upc.femfitai.dtos.CiclosDTOList;
import pe.edu.upc.femfitai.dtos.CiclosDTOUpdate;

public interface ICiclosService {
    CiclosDTOList registrar(CiclosDTOInsert datos);

    List<CiclosDTOList> listar();

    CiclosDTOList buscarPorId(Long id);

    CiclosDTOList actualizar(Long id, CiclosDTOUpdate datos);

    void eliminar(Long id);

    List<CiclosDTOList> listarPorUsuario(Long idUsuario);

    CiclosUsuarioDTO buscarDetallePorId(Long id);
}
