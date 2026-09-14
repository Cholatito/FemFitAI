package pe.edu.upc.femfitai.services.interfaces;

import java.util.List;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTOInsert;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTOUpdate;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTOList;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTODetalle;

public interface ISesionesEntrenamientoService {
    SesionesEntrenamientoDTOList registrar(SesionesEntrenamientoDTOInsert datos);
    List<SesionesEntrenamientoDTOList> listar();
    SesionesEntrenamientoDTOList buscarPorId(Integer id);
    SesionesEntrenamientoDTOList actualizar(Integer id, SesionesEntrenamientoDTOUpdate datos);
    void eliminar(Integer id);
    List<SesionesEntrenamientoDTOList> listarPorUsuario(Integer idUsuario);
    SesionesEntrenamientoDTODetalle buscarDetallePorId(Integer id);
}
