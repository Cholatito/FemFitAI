package pe.edu.upc.femfitai.services.interfaces;

import org.springframework.data.domain.Page;
import pe.edu.upc.femfitai.dtos.*;

public interface IAdministracionService {
    Page<RegistroAdministracionDTO> listar(String modulo, Integer idUsuario, int pagina, int tamano);
    RegistroAdministracionDTO buscar(String modulo, Long id);
    void eliminar(String modulo, Long id);
    CiclosDTO guardarCiclo(Long id, CiclosDTO datos);
    DetalleDiarioCicloDTO guardarDiario(Integer id, DetalleDiarioCicloDTO datos);
    RutinasDTO guardarRutina(Integer id, RutinasDTO datos);
    RutinaEjerciciosDTO guardarAsignacion(Integer id, RutinaEjerciciosDTO datos);
    SesionesEntrenamientoDTO guardarSesion(Integer id, SesionesEntrenamientoDTO datos);
    DetalleSesionDTO guardarDetalle(Integer id, DetalleSesionDTO datos);
    DetalleSerieDTO guardarSerie(Integer id, DetalleSerieDTO datos);
    ProgresoDTO guardarProgreso(Integer id, ProgresoDTO datos);
    PerfilEntrenamientoDTO guardarPerfil(Integer id, PerfilEntrenamientoDTO datos);
    RecomendacionesIADTO guardarRecomendacion(Integer id, RecomendacionesIADTO datos);
}
