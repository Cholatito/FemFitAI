package pe.edu.upc.femfitai.repositories;

import java.util.*;

import org.springframework.data.jpa.repository.*;
import pe.edu.upc.femfitai.entities.DetalleSerie;

public interface DetalleSerieRepository extends JpaRepository<DetalleSerie, Integer> {
    List<DetalleSerie> findByIdDetalleOrderByNumeroSerieAscIdSerieAsc(Integer idDetalle);
    boolean existsByIdDetalle(Integer idDetalle);
    boolean existsByIdDetalleAndNumeroSerie(Integer idDetalle, Integer numeroSerie);
}
