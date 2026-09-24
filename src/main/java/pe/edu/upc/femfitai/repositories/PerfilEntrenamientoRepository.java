package pe.edu.upc.femfitai.repositories;

import java.util.*;

import org.springframework.data.jpa.repository.*;
import pe.edu.upc.femfitai.entities.PerfilEntrenamiento;

public interface PerfilEntrenamientoRepository extends JpaRepository<PerfilEntrenamiento, Integer> {
    Optional<PerfilEntrenamiento> findByIdUsuario(Integer idUsuario);
}
