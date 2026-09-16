package pe.edu.upc.femfitai.services.interfaces;

import pe.edu.upc.femfitai.dtos.CiclosUsuarioDTO;

import java.util.List;
import pe.edu.upc.femfitai.dtos.CiclosDTOInsert;
import pe.edu.upc.femfitai.dtos.CiclosDTO;
import pe.edu.upc.femfitai.dtos.CiclosDTOUpdate;

public interface ICiclosService {
    CiclosDTO registrar(CiclosDTO datos);

    List<CiclosDTO> listar();

    CiclosDTO buscarPorId(Long id);

    CiclosDTO actualizar(Long id, CiclosDTOUpdate datos);

    void eliminar(Long id);

    List<CiclosDTO> listarPorUsuario(Long idUsuario);

    CiclosUsuarioDTO buscarDetallePorId(Long id);
}
