package pe.edu.upc.femfitai.services.interfaces;

import pe.edu.upc.femfitai.dtos.*;

public interface IPerfilEntrenamientoService {
    PerfilEntrenamientoDTO registrar(PerfilEntrenamientoDTO d);
    PerfilEntrenamientoDTO consultar();
    PerfilEntrenamientoDTO actualizar(ActualizarPerfilDTO d);
    PerfilEntrenamientoDTO disponibilidad(DisponibilidadDTO d);
}
