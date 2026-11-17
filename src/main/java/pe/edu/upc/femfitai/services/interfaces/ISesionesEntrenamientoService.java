package pe.edu.upc.femfitai.services.interfaces;

import java.util.List;
import pe.edu.upc.femfitai.dtos.FinalizarSesionDTO;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTO;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTODetalle;

public interface ISesionesEntrenamientoService {
    SesionesEntrenamientoDTO registrar(SesionesEntrenamientoDTO datos);
    SesionesEntrenamientoDTO iniciarDesdeRutina(Integer idRutina);
    SesionesEntrenamientoDTO finalizar(Integer id, FinalizarSesionDTO datos);
    List<SesionesEntrenamientoDTO> listar();
    SesionesEntrenamientoDTO buscarPorId(Integer id);
    SesionesEntrenamientoDTO actualizar(Integer id, SesionesEntrenamientoDTO datos);
    void eliminar(Integer id);
    List<SesionesEntrenamientoDTO> listarPorUsuario(Integer idUsuario);
    SesionesEntrenamientoDTODetalle buscarDetallePorId(Integer id);
}
