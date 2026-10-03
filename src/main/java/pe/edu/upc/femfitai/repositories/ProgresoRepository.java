package pe.edu.upc.femfitai.repositories;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import pe.edu.upc.femfitai.entities.Progreso;

public interface ProgresoRepository extends JpaRepository<Progreso, Integer> {
    Page<Progreso> findByUsuario_IdUsuarioOrderByFechaDescIdProgresoDesc(Integer idUsuario, Pageable pageable);
}
