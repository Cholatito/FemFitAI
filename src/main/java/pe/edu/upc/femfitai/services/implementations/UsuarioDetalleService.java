package pe.edu.upc.femfitai.services.implementations;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.femfitai.dtos.PerfilEntrenamientoDTO;
import pe.edu.upc.femfitai.dtos.UsuarioDetalleDTO;
import pe.edu.upc.femfitai.entities.Usuarios;
import pe.edu.upc.femfitai.repositories.*;
import pe.edu.upc.femfitai.services.interfaces.IUsuarioDetalleService;

@Service
public class UsuarioDetalleService implements IUsuarioDetalleService {
    private final UsuariosRepository usuarios;
    private final PerfilEntrenamientoRepository perfiles;
    private final RutinasRepository rutinas;
    private final SesionesEntrenamientoRepository sesiones;
    private final ProgresoRepository progreso;
    private final CiclosRepository ciclos;

    public UsuarioDetalleService(UsuariosRepository usuarios,
                                 PerfilEntrenamientoRepository perfiles,
                                 RutinasRepository rutinas,
                                 SesionesEntrenamientoRepository sesiones,
                                 ProgresoRepository progreso,
                                 CiclosRepository ciclos) {
        this.usuarios = usuarios;
        this.perfiles = perfiles;
        this.rutinas = rutinas;
        this.sesiones = sesiones;
        this.progreso = progreso;
        this.ciclos = ciclos;
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDetalleDTO detalle(Integer id) {
        Usuarios u = usuarios.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un usuario con ID " + id));

        PerfilEntrenamientoDTO perfil = perfiles.findByUsuario_IdUsuario(id)
                .map(p -> new PerfilEntrenamientoDTO(p.getIdPerfil(), id,
                        p.getNivelEntrenamiento(), p.getObjetivoPrincipal(),
                        p.getDiasDisponibles(), p.getTiempoDisponible(), p.getFechaNacimiento()))
                .orElse(null);

        return new UsuarioDetalleDTO(u.getIdUsuario(), u.getNombres(), u.getApellidos(),
                u.getCorreo(), u.getRol(), u.getEstado(), u.getFechaRegistro(),
                perfil,
                rutinas.countByUsuario_IdUsuario(id),
                sesiones.countByUsuario_IdUsuario(id),
                progreso.countByUsuario_IdUsuario(id),
                ciclos.countByUsuario_IdUsuario(id));
    }
}