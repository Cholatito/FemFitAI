package pe.edu.upc.femfitai.securities;

import java.util.Locale;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.edu.upc.femfitai.entities.Usuarios;
import pe.edu.upc.femfitai.repositories.UsuariosRepository;

@Service
public class UsuariosUserDetailsService implements UserDetailsService {
    private final UsuariosRepository usuariosRepository;

    public UsuariosUserDetailsService(UsuariosRepository usuariosRepository) {
        this.usuariosRepository = usuariosRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) {
        Usuarios usuario = usuariosRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));

        String rol = usuario.getRol() == null ? "USUARIA"
                : usuario.getRol().trim().toUpperCase(Locale.ROOT);
        if (!rol.equals("USUARIA") && !rol.equals("ADMIN")) {
            throw new UsernameNotFoundException("Rol de usuario no permitido");
        }

        return User.withUsername(usuario.getCorreo())
                .password(usuario.getPasswordHash())
                .authorities("ROLE_" + rol)
                .disabled(!Boolean.TRUE.equals(usuario.getEstado()))
                .build();
    }
}
