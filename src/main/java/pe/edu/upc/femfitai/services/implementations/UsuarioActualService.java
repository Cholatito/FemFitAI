package pe.edu.upc.femfitai.services.implementations;

import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.femfitai.entities.Usuarios;
import pe.edu.upc.femfitai.repositories.UsuariosRepository;

@Service
public class UsuarioActualService {
    private final UsuariosRepository usuarios;
    public UsuarioActualService(UsuariosRepository usuarios) { this.usuarios = usuarios; }
    public Usuarios obtener() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticacion requerida");
        return usuarios.findByCorreo(auth.getName()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario autenticado no disponible"));
    }
    public Integer id() { return obtener().getIdUsuario(); }
    public void verificar(Number propietario) {
        if (propietario == null || propietario.longValue() != id().longValue())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El recurso pertenece a otra cuenta");
    }
}
