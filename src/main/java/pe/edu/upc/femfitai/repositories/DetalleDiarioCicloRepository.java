package pe.edu.upc.femfitai.repositories;

import java.util.*;
import java.time.LocalDate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.femfitai.entities.DetalleDiarioCiclo;

public interface DetalleDiarioCicloRepository extends JpaRepository<DetalleDiarioCiclo, Integer> {
    boolean existsByIdCiclo(Integer idCiclo);
    @Query("""
        select d from DetalleDiarioCiclo d join Ciclos c on d.idCiclo = c.idCiclo
        where c.idUsuario = :idUsuario and d.fecha = :fecha order by d.idDetalleDiarioCiclo
        """)
    List<DetalleDiarioCiclo> buscarPorUsuarioYFecha(@Param("idUsuario") Long idUsuario, @Param("fecha") LocalDate fecha);
    @Query("""
        select d from DetalleDiarioCiclo d join Ciclos c on d.idCiclo = c.idCiclo
        where c.idUsuario = :idUsuario order by d.fecha desc, d.idDetalleDiarioCiclo desc
        """)
    Page<DetalleDiarioCiclo> historial(@Param("idUsuario") Long idUsuario, Pageable pageable);
}
