package pe.edu.upc.femfitai.services.implementations;

import pe.edu.upc.femfitai.services.interfaces.IUsuariosService;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.femfitai.dtos.UsuariosDTO;
import pe.edu.upc.femfitai.entities.Usuarios;
import pe.edu.upc.femfitai.repositories.UsuariosRepository;

@Service
public class UsuariosService implements IUsuariosService {
    private final UsuariosRepository repository;

    public UsuariosService(UsuariosRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public UsuariosDTO registrar(UsuariosDTO datos) {
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos son obligatorios");
        }
        validar(datos.getNombres(), datos.getApellidos(), datos.getCorreo(), datos.getPasswordHash());
        Usuarios usuario = new Usuarios(datos.getNombres(), datos.getApellidos(),
                datos.getCorreo(), datos.getPasswordHash(),
                datos.getRol() == null ? "USUARIA" : datos.getRol(),
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
        usuario.setNombres(datos.getNombres());
        usuario.setApellidos(datos.getApellidos());
        usuario.setCorreo(datos.getCorreo());
        usuario.setPasswordHash(datos.getPasswordHash());
        usuario.setRol(datos.getRol());
        usuario.setEstado(datos.getEstado());
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

    private UsuariosDTO convertirADTO(Usuarios usuario) {
        return new UsuariosDTO(usuario.getIdUsuario(), usuario.getNombres(),
                usuario.getApellidos(), usuario.getCorreo(), usuario.getRol(),
                usuario.getEstado(), usuario.getFechaRegistro());
    }
}