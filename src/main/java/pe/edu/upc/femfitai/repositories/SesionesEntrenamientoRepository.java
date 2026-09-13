package pe.edu.upc.femfitai.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTODetalle;
import pe.edu.upc.femfitai.entities.SesionesEntrenamiento;

public interface SesionesEntrenamientoRepository extends JpaRepository<SesionesEntrenamiento, Integer> {
    List<SesionesEntrenamiento> findByIdUsuario(Integer idUsuario);

    @Query("""
            select new pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTODetalle(
                s.idSesion, s.idUsuario, u.nombres, u.apellidos,
                s.idRutina, r.nombre, s.fecha, s.duracionMin,
                s.nivelEnergia, s.esfuerzoPercibido, s.estado)
            from SesionesEntrenamiento s
            join Usuarios u on s.idUsuario = u.idUsuario
            join Rutinas r on s.idRutina = r.idRutina
            where s.idSesion = :id
            """)
    Optional<SesionesEntrenamientoDTODetalle> buscarDetallePorId(@Param("id") Integer id);
}
