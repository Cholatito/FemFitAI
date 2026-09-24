package pe.edu.upc.femfitai.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.femfitai.dtos.EjerciciosUsuarioDTO;
import pe.edu.upc.femfitai.entities.Ejercicios;

public interface EjerciciosRepository extends JpaRepository<Ejercicios, Integer> {
    @Query("""
            select e from Ejercicios e
            where lower(e.grupoMuscular) = lower(:grupoMuscular)
            order by e.nombre, e.idEjercicio
            """)
    org.springframework.data.domain.Page<Ejercicios> buscar(
            @Param("tipo") String tipo, @Param("query") String query,
            org.springframework.data.domain.Pageable pageable);
    @Query("""
            select e from Ejercicios e
            where lower(e.grupoMuscular) = lower(:grupoMuscular)
            order by e.nombre, e.idEjercicio
            """)
    List<Ejercicios> buscarPorGrupoMuscular(@Param("grupoMuscular") String grupoMuscular);

    @Query("""
            select new pe.edu.upc.femfitai.dtos.EjerciciosUsuarioDTO(
                u.idUsuario, u.nombres, u.apellidos,
                r.idRutina, r.nombre, re.idRutinaEjercicio,
                e.idEjercicio, e.nombre, e.grupoMuscular, e.tipo,
                re.series, re.repeticiones, re.descansoSeg)
            from Usuarios u
            join Rutinas r on r.idUsuario = u.idUsuario
            join RutinaEjercicios re on re.idRutina = r.idRutina
            join Ejercicios e on e.idEjercicio = re.idEjercicio
            where u.idUsuario = :idUsuario
            order by r.idRutina, re.idRutinaEjercicio
            """)
    List<EjerciciosUsuarioDTO> listarPorUsuario(@Param("idUsuario") Integer idUsuario);
}
