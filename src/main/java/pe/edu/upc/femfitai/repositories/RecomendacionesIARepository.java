package pe.edu.upc.femfitai.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.femfitai.entities.RecomendacionesIA;

public interface RecomendacionesIARepository extends JpaRepository<RecomendacionesIA, Integer> {
    Page<RecomendacionesIA> findByUsuario_IdUsuarioOrderByFechaDescIdRecomendacionDesc(Integer idUsuario, Pageable pageable);

    org.springframework.data.domain.Page<RecomendacionesIA> findByUsuario_IdUsuario(Integer idUsuario, org.springframework.data.domain.Pageable pageable);
    boolean existsByRutina_IdRutina(Integer idRutina);
}
