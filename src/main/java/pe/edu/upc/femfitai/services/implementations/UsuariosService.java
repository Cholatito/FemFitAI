package pe.edu.upc.femfitai.services.implementations;

import pe.edu.upc.femfitai.repositories.IUsersRepository;
import pe.edu.upc.femfitai.repositories.UsuariosRepository;
import pe.edu.upc.femfitai.services.interfaces.IUsuariosService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
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
        Usuarios usuario = new Usuarios(datos.getNombres(), datos.getApellidos(),
                datos.getCorreo(), passwordEncoder.encode(datos.getPasswordHash()),
                "USUARIA",
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
        var solicitante = actual.obtener();
        if (!java.util.Set.of("ADMIN", "PROGRAMADOR").contains(String.valueOf(solicitante.getRol()).trim().toUpperCase(Locale.ROOT))) {
            actual.verificar(id);
        }
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
        if (passwordHash.length() < 8
                || passwordHash.chars().noneMatch(Character::isUpperCase)
                || passwordHash.chars().noneMatch(Character::isLowerCase)
                || passwordHash.chars().noneMatch(Character::isDigit)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número");
        }
    }

    private void obligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, campo + " es obligatorio");
        }
    }

    private String normalizarRol(String rol) {
        String rolNormalizado = rol.trim().toUpperCase(Locale.ROOT);
        if (!java.util.Set.of("USUARIA", "ADMIN", "TESTER", "PROGRAMADOR").contains(rolNormalizado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Rol debe ser USUARIA, ADMIN, TESTER o PROGRAMADOR");
        }
        return rolNormalizado;
    }

    private UsuariosDTO convertirADTO(Usuarios usuario) {
        return new UsuariosDTO(usuario.getIdUsuario(), usuario.getNombres(),
                usuario.getApellidos(), usuario.getCorreo(), usuario.getRol(),
                usuario.getEstado(), usuario.getFechaRegistro());
    }
}
