package pe.edu.upc.femfitai.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.femfitai.entities.Usuarios;

public interface UsuariosRepository extends JpaRepository<Usuarios, Integer> {
}