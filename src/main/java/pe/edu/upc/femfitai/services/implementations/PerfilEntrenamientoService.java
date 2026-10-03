package pe.edu.upc.femfitai.services.implementations;

import java.time.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.femfitai.dtos.*;
import pe.edu.upc.femfitai.entities.*;
import pe.edu.upc.femfitai.repositories.*;
import pe.edu.upc.femfitai.services.interfaces.IPerfilEntrenamientoService;
import static pe.edu.upc.femfitai.services.implementations.Validaciones.*;

@Service
public class PerfilEntrenamientoService implements IPerfilEntrenamientoService {
    private final PerfilEntrenamientoRepository repository;
    private final UsuariosRepository usuarios;
    private final UsuarioActualService actual;
    public PerfilEntrenamientoService(PerfilEntrenamientoRepository repository, UsuariosRepository usuarios, UsuarioActualService actual) {
        this.repository = repository;
        this.usuarios = usuarios;
        this.actual = actual;
    }
    @Transactional
    public PerfilEntrenamientoDTO registrar(PerfilEntrenamientoDTO d) {
        exigir(d != null, "Los datos son obligatorios");
        Integer usuario = actual.id();
        if (d.idUsuario() != null) actual.verificar(d.idUsuario());
        var usuarioObj = usuarios.bloquear(usuario).orElseThrow(() -> noEncontrado("Usuario"));
        conflicto(repository.findByUsuario_IdUsuario(usuario).isPresent(), "La usuaria ya tiene un perfil");
        validarPerfil(d.nivelEntrenamiento(), d.objetivoPrincipal());
        validarDisponibilidad(d.diasDisponibles(), d.tiempoDisponible());
        exigir(d.fechaNacimiento() != null && !d.fechaNacimiento().isAfter(LocalDate.now()), "FechaNacimiento obligatoria y no futura");
        return dto(repository.save(new PerfilEntrenamiento(usuarioObj, d.nivelEntrenamiento(), d.objetivoPrincipal(), d.diasDisponibles(), d.tiempoDisponible(),d.fechaNacimiento())));
    }
    @Transactional(readOnly = true)
    public PerfilEntrenamientoDTO consultar() { return dto(propio()); }
    @Transactional
    public PerfilEntrenamientoDTO actualizar(ActualizarPerfilDTO d) {
        exigir(d != null, "Los datos son obligatorios");
        validarPerfil(d.nivelEntrenamiento(), d.objetivoPrincipal());
        var e = propio();
        e.setNivelEntrenamiento(d.nivelEntrenamiento());
        e.setObjetivoPrincipal(d.objetivoPrincipal());
        return dto(repository.save(e));
    }
    @Transactional
    public PerfilEntrenamientoDTO disponibilidad(DisponibilidadDTO d) {
        exigir(d != null, "Los datos son obligatorios");
        validarDisponibilidad(d.diasDisponibles(), d.tiempoDisponible());
        var e = propio();
        e.setDiasDisponibles(d.diasDisponibles());
        e.setTiempoDisponible(d.tiempoDisponible());
        return dto(repository.save(e));
    }
    private PerfilEntrenamiento propio() {
        return repository.findByUsuario_IdUsuario(actual.id()).orElseThrow(() -> noEncontrado("Perfil"));
    }
    private void validarPerfil(String nivel, String objetivo) {
        texto(nivel, 50, "NivelEntrenamiento", true);
        texto(objetivo, 100, "ObjetivoPrincipal", true);
    }
    private void validarDisponibilidad(Integer dias, Integer tiempo) {
        exigir(dias != null && dias >= 1 && dias <= 7, "DiasDisponibles debe estar entre 1 y 7");
        positivo(tiempo, "TiempoDisponible");
    }
    private PerfilEntrenamientoDTO dto(PerfilEntrenamiento e) {
// Extraemos el ID navegando por el objeto Usuarios de forma segura
        Integer idUsuario = (e.getUsuario() != null) ? e.getUsuario().getIdUsuario() : null;

        return new PerfilEntrenamientoDTO(
                e.getIdPerfil(),
                idUsuario,
                e.getNivelEntrenamiento(),
                e.getObjetivoPrincipal(),
                e.getDiasDisponibles(),
                e.getTiempoDisponible(),
                e.getFechaNacimiento()
        );    }
}