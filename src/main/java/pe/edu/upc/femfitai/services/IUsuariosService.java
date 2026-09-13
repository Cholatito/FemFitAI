package pe.edu.upc.femfitai.services;

import java.util.List;
import pe.edu.upc.femfitai.dtos.UsuariosDTOInsert;
import pe.edu.upc.femfitai.dtos.UsuariosDTOUpdate;
import pe.edu.upc.femfitai.dtos.UsuariosDTOList;

public interface IUsuariosService {
    UsuariosDTOList registrar(UsuariosDTOInsert datos);
    List<UsuariosDTOList> listar();
    UsuariosDTOList buscarPorId(Integer id);
    UsuariosDTOList actualizar(Integer id, UsuariosDTOUpdate datos);
    void eliminar(Integer id);
}