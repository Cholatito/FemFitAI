package pe.edu.upc.femfitai.services.implementations;

import java.time.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import pe.edu.upc.femfitai.dtos.*;
import pe.edu.upc.femfitai.entities.*;
import pe.edu.upc.femfitai.repositories.*;
import pe.edu.upc.femfitai.services.interfaces.IProgresoService;
import static pe.edu.upc.femfitai.services.implementations.Validaciones.*;

@Service
public class ProgresoService implements IProgresoService {
    private final ProgresoRepository repository;
    private final UsuarioActualService actual;
    public ProgresoService(ProgresoRepository repository, UsuarioActualService actual) {
        this.repository = repository;
        this.actual = actual;
    }
    @Transactional
    public ProgresoDTO registrar(ProgresoDTO d) {
        exigir(d != null, "Los datos son obligatorios");
        Integer usuario = actual.id();
        if (d.idUsuario() != null) actual.verificar(d.idUsuario());
        decimal(d.pesoKg(), "PesoKg", true, false);
        decimal(d.medidaOpcional(), "MedidaOpcional", false, false);
        texto(d.notaPersonal(), 255, "NotaPersonal", false);
        LocalDate fecha = d.fecha() == null ? LocalDate.now() : d.fecha();
        exigir(!fecha.isAfter(LocalDate.now()), "Fecha no puede ser futura");
        return dto(repository.save(new Progreso(usuario, fecha, d.pesoKg(), d.medidaOpcional(), d.notaPersonal())));
    }
    @Transactional(readOnly = true)
    public Page<ProgresoDTO> listar(int pagina, int tamano) {
        return repository.findByIdUsuarioOrderByFechaDescIdProgresoDesc(actual.id(), paginar(pagina, tamano)).map(this::dto);
    }
    @Transactional(readOnly = true)
    public ProgresoDTO buscarPorId(Integer id) {
        positivo(id, "IdProgreso");
        var e = repository.findById(id).orElseThrow(() -> noEncontrado("Progreso"));
        actual.verificar(e.getIdUsuario());
        return dto(e);
    }
    private ProgresoDTO dto(Progreso e) {
        return new ProgresoDTO(e.getIdProgreso(), e.getIdUsuario(), e.getFecha(), e.getPesoKg(), e.getMedidaOpcional(), e.getNotaPersonal());
    }
}
