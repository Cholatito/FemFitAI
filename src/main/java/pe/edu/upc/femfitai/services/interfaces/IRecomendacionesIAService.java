package pe.edu.upc.femfitai.services.interfaces;

import org.springframework.data.domain.Page;
import pe.edu.upc.femfitai.dtos.RecomendacionesIADTO;

public interface IRecomendacionesIAService {
    Page<RecomendacionesIADTO> listar(int pagina, int tamano);
    RecomendacionesIADTO buscarPorId(Integer id);
    RecomendacionesIADTO aceptar(Integer id);
}
