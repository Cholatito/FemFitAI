package pe.edu.upc.femfitai.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.femfitai.entities.Usuarios;

public interface UsuariosRepository extends JpaRepository<Usuarios, Integer> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from Usuarios u where u.idUsuario = :id")
    Optional<Usuarios> bloquear(@org.springframework.data.repository.query.Param("id") Integer id);
    Optional<Usuarios> findByCorreo(String correo);
}
