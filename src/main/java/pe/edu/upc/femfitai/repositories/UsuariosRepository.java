package pe.edu.upc.femfitai.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.femfitai.entities.Usuarios;

public interface UsuariosRepository extends JpaRepository<Usuarios, Integer> {
    Optional<Usuarios> findByCorreo(String correo);
}
