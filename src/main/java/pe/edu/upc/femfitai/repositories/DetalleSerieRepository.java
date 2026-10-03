package pe.edu.upc.femfitai.repositories;

import java.util.*;

import org.springframework.data.jpa.repository.*;
import pe.edu.upc.femfitai.entities.DetalleSerie;

public interface DetalleSerieRepository extends JpaRepository<DetalleSerie, Integer> {
    List<DetalleSerie> findByDetalle_IdDetalleOrderByNumeroSerieAscIdSerieAsc(Integer idDetalle);
    boolean existsByDetalle_IdDetalle(Integer idDetalle);
    boolean existsByDetalle_IdDetalleAndNumeroSerie(Integer idDetalle, Integer numeroSerie);
}
