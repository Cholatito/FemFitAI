package pe.edu.upc.femfitai.services.implementations;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pe.edu.upc.femfitai.repositories.IUsersRepository;
import pe.edu.upc.femfitai.repositories.UsuariosRepository;
import pe.edu.upc.femfitai.services.interfaces.IUsuariosService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.femfitai.dtos.UsuariosDTO;
import pe.edu.upc.femfitai.entities.Usuarios;
import pe.edu.upc.femfitai.repositories.IUsersRepository;

@Service
public class UsuariosService implements IUsuariosService {
    private final UsuariosRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioActualService actual;
    private static final Set<String> ROLES_PERMITIDOS = Set.of("PROGRAMADOR", "TESTER");
    private static final String ROL_POR_DEFECTO = "TESTER";


    public UsuariosService(UsuariosRepository repository, PasswordEncoder passwordEncoder, UsuarioActualService actual) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.actual = actual;
    }

    @Override
    @Transactional
    public UsuariosDTO registrar(UsuariosDTO datos) {
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos son obligatorios");
        }
        validar(datos.getNombres(), datos.getApellidos(), datos.getCorreo(), datos.getPasswordHash());
        if (repository.findByCorreo(datos.getCorreo()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        String rol = resolverRolAlRegistrar(datos.getRol());
        Usuarios usuario = new Usuarios(datos.getNombres(), datos.getApellidos(),
                datos.getCorreo(), passwordEncoder.encode(datos.getPasswordHash()),
                rol,
                datos.getEstado() == null ? true : datos.getEstado(), LocalDateTime.now());
        return convertirADTO(repository.save(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuariosDTO> listar() {
        return repository.findAll().stream().map(this::convertirADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuariosDTO buscarPorId(Integer id) {
        return convertirADTO(obtenerUsuario(id));
    }

    @Override
    @Transactional
    public UsuariosDTO actualizar(Integer id, UsuariosDTO datos) {
        Usuarios usuario = obtenerUsuario(id);
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos son obligatorios");
        }
        validar(datos.getNombres(), datos.getApellidos(), datos.getCorreo(), datos.getPasswordHash());
        repository.findByCorreo(datos.getCorreo())
                .filter(existente -> !existente.getIdUsuario().equals(id))
                .ifPresent(existente -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "El correo ya está registrado");
                });
        usuario.setNombres(datos.getNombres());
        usuario.setApellidos(datos.getApellidos());
        usuario.setCorreo(datos.getCorreo());
        usuario.setPasswordHash(passwordEncoder.encode(datos.getPasswordHash()));
        usuario.setRol(datos.getRol() == null ? usuario.getRol() : normalizarRol(datos.getRol()));
        usuario.setEstado(datos.getEstado() == null ? usuario.getEstado() : datos.getEstado());
        return convertirADTO(repository.save(usuario));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        repository.delete(obtenerUsuario(id));
    }

    private Usuarios obtenerUsuario(Integer id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un usuario con ID " + id);
        }
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un usuario con ID " + id));
    }

    private void validar(String nombres, String apellidos, String correo, String passwordHash) {
        obligatorio(nombres, "Nombres");
        obligatorio(apellidos, "Apellidos");
        obligatorio(correo, "Correo");
        obligatorio(passwordHash, "PasswordHash");
    }

    private void obligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, campo + " es obligatorio");
        }
    }

    private String normalizarRol(String rol) {
        String rolNormalizado = rol.trim().toUpperCase(Locale.ROOT);
        if (!ROLES_PERMITIDOS.contains(rolNormalizado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Rol debe ser PROGRAMADOR o TESTER");
        }
        return rolNormalizado;
    }
    private String resolverRolAlRegistrar(String rolSolicitado) {
        if (rolSolicitado == null || rolSolicitado.isBlank()) {
            return ROL_POR_DEFECTO;
        }
        String rol = normalizarRol(rolSolicitado);
        if (rol.equals("PROGRAMADOR") && !quienLlamaEsProgramador()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo un PROGRAMADOR puede crear otro PROGRAMADOR");
        }
        return rol;
    }

    private boolean quienLlamaEsProgramador() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
                && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROGRAMADOR"));
    }
    private UsuariosDTO convertirADTO(Usuarios usuario) {
        return new UsuariosDTO(usuario.getIdUsuario(), usuario.getNombres(),
                usuario.getApellidos(), usuario.getCorreo(), usuario.getRol(),
                usuario.getEstado(), usuario.getFechaRegistro());
    }
}
