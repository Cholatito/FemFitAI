package pe.edu.upc.femfitai.repositories;

import pe.edu.upc.femfitai.dtos.CiclosUsuarioDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.femfitai.entities.Ciclos;

public interface CiclosRepository extends JpaRepository<Ciclos, Long> {

    java.util.List<Ciclos> findByUsuario_IdUsuario(Long idUsuario);
    java.util.List<Ciclos> findByUsuario_IdUsuarioOrderByFechaInicioDescIdCicloDesc(Long idUsuario);
    boolean existsByUsuario_IdUsuarioAndFechaRealIsNull(Long idUsuario);
    boolean existsByUsuario_IdUsuarioAndFechaRealIsNullAndIdCicloNot(Long idUsuario, Long idCiclo);

    @Query("""
            select new pe.edu.upc.femfitai.dtos.CiclosUsuarioDTO(
                c.idCiclo, u.idUsuario, u.nombres, u.apellidos,
                c.fechaInicio, c.fechaFinEstimada, c.fechaReal)
            from Ciclos c
            join c.usuario u
            where c.idCiclo = :id
            """)
    Optional<CiclosUsuarioDTO> buscarDetallePorId(
            @Param("id") Long id);
}
