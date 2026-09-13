package pe.edu.upc.femfitai.repositories;

import pe.edu.upc.femfitai.dtos.CiclosUsuarioDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.femfitai.entities.Ciclos;

public interface CiclosRepository extends JpaRepository<Ciclos, Long> {

    java.util.List<Ciclos> findByIdUsuario(Long idUsuario);

    @Query("""
            select new pe.edu.upc.femfitai.dtos.CiclosUsuarioDTO(
                c.idCiclo, c.idUsuario, u.nombres, u.apellidos,
                c.fechaInicio, c.fechaFinEstimada, c.fechaReal)
            from Ciclos c
            join Usuarios u on c.idUsuario = u.idUsuario
            where c.idCiclo = :id
            """)
    Optional<CiclosUsuarioDTO> buscarDetallePorId(
            @Param("id") Long id);
}
