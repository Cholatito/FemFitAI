package pe.edu.upc.femfitai.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.femfitai.dtos.RutinasUsuarioDTO;
import pe.edu.upc.femfitai.entities.Rutinas;

public interface RutinasRepository extends JpaRepository<Rutinas, Integer> {
    List<Rutinas> findByIdUsuario(Integer idUsuario);

    @Query("""
            select new pe.edu.upc.femfitai.dtos.RutinasUsuarioDTO(
                r.idRutina, r.idUsuario, u.nombres, u.apellidos,
                r.nombre, r.objetivo, r.nivel, r.fechaCreacion, r.estado)
            from Rutinas r
            join Usuarios u on r.idUsuario = u.idUsuario
            where r.idRutina = :id
            """)
    Optional<RutinasUsuarioDTO> buscarDetallePorId(@Param("id") Integer id);
}