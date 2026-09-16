package pe.edu.upc.femfitai.services.interfaces;

import java.util.List;
import pe.edu.upc.femfitai.dtos.UsuariosDTO;

public interface IUsuariosService {
    UsuariosDTO registrar(UsuariosDTO datos);
    List<UsuariosDTO> listar();
    UsuariosDTO buscarPorId(Integer id);
    UsuariosDTO actualizar(Integer id, UsuariosDTO datos);
    void eliminar(Integer id);
}