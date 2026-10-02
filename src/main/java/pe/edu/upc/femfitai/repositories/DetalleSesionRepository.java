package pe.edu.upc.femfitai.repositories;

import java.util.*;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.femfitai.dtos.DetalleSesionEjercicioDTO;
import pe.edu.upc.femfitai.entities.DetalleSesion;

public interface DetalleSesionRepository extends JpaRepository<DetalleSesion, Integer> {
    boolean existsByIdEjercicio(Integer idEjercicio);
    @Query("""
        select new pe.edu.upc.femfitai.dtos.DetalleSesionEjercicioDTO(
            d.idDetalle, d.idSesion, e.idEjercicio, e.nombre, e.grupoMuscular, d.observacion)
        from DetalleSesion d join Ejercicios e on d.idEjercicio = e.idEjercicio
        where d.idSesion = :idSesion order by d.idDetalle
        """)
    List<DetalleSesionEjercicioDTO> listarDetalle(@Param("idSesion") Integer idSesion);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DetalleSesion d where d.idDetalle = :id")
    Optional<DetalleSesion> bloquear(@Param("id") Integer id);
}
