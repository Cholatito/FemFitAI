package pe.edu.upc.femfitai.services.implementations;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

@Service
public class UsuariosService implements IUsuariosService {
    private final UsuariosRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioActualService actual;
    private static final Set<String> ROLES_PERMITIDOS = Set.of("ADMIN", "USUARIA");
    private static final String ROL_POR_DEFECTO = "USUARIA";
    private static final String PATRON_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";


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
        if (!quienLlamaEsAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para listar usuarios");
        }
        return repository.findAll().stream().map(this::convertirADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuariosDTO buscarPorId(Integer id) {
        Usuarios usuario = obtenerUsuario(id);
        if (!quienLlamaEsAdmin()) {
            actual.verificar(usuario.getIdUsuario());
        }
        return convertirADTO(usuario);
    }

    @Override
    @Transactional
    public UsuariosDTO actualizar(Integer id, UsuariosDTO datos) {
        Usuarios usuario = obtenerUsuario(id);
        if (!quienLlamaEsAdmin()) {
            actual.verificar(usuario.getIdUsuario());
        }
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos son obligatorios");
        }
        validar(datos.getNombres(), datos.getApellidos(), datos.getCorreo());
        boolean cambiaPassword = datos.getPasswordHash() != null && !datos.getPasswordHash().isBlank();
        if (cambiaPassword) {
            validarPassword(datos.getPasswordHash());
        }
        repository.findByCorreo(datos.getCorreo())
                .filter(existente -> !existente.getIdUsuario().equals(id))
                .ifPresent(existente -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "El correo ya está registrado");
                });
        usuario.setNombres(datos.getNombres());
        usuario.setApellidos(datos.getApellidos());
        usuario.setCorreo(datos.getCorreo());
        if (cambiaPassword) {
            usuario.setPasswordHash(passwordEncoder.encode(datos.getPasswordHash()));
        }
        usuario.setEstado(datos.getEstado() == null ? usuario.getEstado() : datos.getEstado());
        return convertirADTO(repository.save(usuario));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Usuarios usuario = obtenerUsuario(id);
        if (!quienLlamaEsAdmin()) {
            actual.verificar(usuario.getIdUsuario());
        }
        usuario.setEstado(false);
        repository.save(usuario);
    }

    private Usuarios obtenerUsuario(Integer id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un usuario con ID " + id);
        }
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un usuario con ID " + id));
    }

    private void validar(String nombres, String apellidos, String correo) {
        obligatorio(nombres, "Nombres");
        obligatorio(apellidos, "Apellidos");
        obligatorio(correo, "Correo");
    }

    private void validar(String nombres, String apellidos, String correo, String passwordHash) {
        validar(nombres, apellidos, correo);
        validarPassword(passwordHash);
    }

    private void validarPassword(String passwordHash) {
        obligatorio(passwordHash, "PasswordHash");
        if (!passwordHash.matches(PATRON_PASSWORD)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener al menos 8 caracteres, incluir mayúsculas, minúsculas y números");
        }
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
                    "Rol debe ser ADMIN o USUARIA");
        }
        return rolNormalizado;
    }
    private String resolverRolAlRegistrar(String rolSolicitado) {
        if (rolSolicitado == null || rolSolicitado.isBlank()) {
            return ROL_POR_DEFECTO;
        }
        String rol = normalizarRol(rolSolicitado);
        if (rol.equals("ADMIN") && !quienLlamaEsAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo un ADMIN puede crear otro ADMIN");
        }
        return rol;
    }

    private boolean quienLlamaEsAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
                && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));    }
    private UsuariosDTO convertirADTO(Usuarios usuario) {
        return new UsuariosDTO(usuario.getIdUsuario(), usuario.getNombres(),
                usuario.getApellidos(), usuario.getCorreo(), usuario.getRol(),
                usuario.getEstado(), usuario.getFechaRegistro());
    }
}
