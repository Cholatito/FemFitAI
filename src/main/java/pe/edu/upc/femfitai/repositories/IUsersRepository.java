package pe.edu.upc.femfitai.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.femfitai.entities.Usuarios;

import java.util.Optional;

@Repository
public interface IUsersRepository extends JpaRepository<Usuarios, Integer> {

    Optional<Usuarios> findBynombres(String nombres);
}
