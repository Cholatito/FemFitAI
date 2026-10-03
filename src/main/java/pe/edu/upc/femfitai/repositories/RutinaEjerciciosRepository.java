package pe.edu.upc.femfitai.repositories;

import java.util.*;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.femfitai.entities.RutinaEjercicios;
import pe.edu.upc.femfitai.dtos.*;

public interface RutinaEjerciciosRepository extends JpaRepository<RutinaEjercicios, Integer> {
    boolean existsByEjercicio_IdEjercicio(Integer idEjercicio);
    @Query("""
        select new pe.edu.upc.femfitai.dtos.RutinaEjercicioDetalleDTO(
            re.idRutinaEjercicio, re.rutina.idRutina, e.idEjercicio, e.nombre, e.grupoMuscular,
            e.tipo, e.descripcion, re.series, re.repeticiones, re.descansoSeg)
        from RutinaEjercicios re join re.ejercicio e
        where re.rutina.idRutina = :idRutina order by re.idRutinaEjercicio
        """)
    List<RutinaEjercicioDetalleDTO> listarDetalle(@Param("idRutina") Integer idRutina);
}
