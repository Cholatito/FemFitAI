package pe.edu.upc.femfitai.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.femfitai.entities.DetalleDiarioCiclo;

import java.util.List;

public interface DetalleDiarioCicloRepository extends JpaRepository<DetalleDiarioCiclo, Integer> {

    List<DetalleDiarioCiclo> findByCiclo_IdCicloOrderByFechaAsc(Long idCiclo);
}