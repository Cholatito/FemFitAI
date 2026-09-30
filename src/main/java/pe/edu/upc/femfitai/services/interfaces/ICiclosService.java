package pe.edu.upc.femfitai.services.interfaces;

import pe.edu.upc.femfitai.dtos.CiclosUsuarioDTO;

import java.util.List;

import pe.edu.upc.femfitai.dtos.CiclosDTO;
import pe.edu.upc.femfitai.dtos.CiclosRequestDTO;

public interface ICiclosService {
    CiclosDTO registrar(CiclosRequestDTO datos);

    List<CiclosDTO> listar();

    CiclosDTO buscarPorId(Long id);

    CiclosDTO actualizar(Long id, CiclosRequestDTO datos);

    void eliminar(Long id);

    List<CiclosDTO> listarPorUsuario(Integer idUsuario);

    CiclosUsuarioDTO buscarDetallePorId(Long id);
}