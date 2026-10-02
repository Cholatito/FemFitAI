package pe.edu.upc.femfitai.repositories;

import java.util.*;
import java.time.LocalDate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.femfitai.entities.DetalleDiarioCiclo;

public interface DetalleDiarioCicloRepository extends JpaRepository<DetalleDiarioCiclo, Integer> {
    boolean existsByCiclo_IdCiclo(Long idCiclo);

    // Navegamos directamente al objeto "ciclo" y luego al "usuario" de ese ciclo
    @Query("""
        select d from DetalleDiarioCiclo d 
        join d.ciclo c 
        where c.usuario.idUsuario = :idUsuario and d.fecha = :fecha 
        order by d.idDetalleDiarioCiclo
        """)
    List<DetalleDiarioCiclo> buscarPorUsuarioYFecha(@Param("idUsuario") Long idUsuario, @Param("fecha") LocalDate fecha);

    @Query("""
        select d from DetalleDiarioCiclo d 
        join d.ciclo c 
        where c.usuario.idUsuario = :idUsuario 
        order by d.fecha desc, d.idDetalleDiarioCiclo desc
        """)
    Page<DetalleDiarioCiclo> historial(@Param("idUsuario") Long idUsuario, Pageable pageable);
    List<DetalleDiarioCiclo> findByCiclo_IdCicloOrderByFechaAsc(Long idCiclo);
}
