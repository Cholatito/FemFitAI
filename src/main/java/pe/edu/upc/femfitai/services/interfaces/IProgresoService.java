package pe.edu.upc.femfitai.services.interfaces;

import org.springframework.data.domain.Page;
import pe.edu.upc.femfitai.dtos.*;

public interface IProgresoService {
    ProgresoDTO registrar(ProgresoDTO d);
    Page<ProgresoDTO> listar(int pagina, int tamano);
    ProgresoDTO buscarPorId(Integer id);
}
