package pe.edu.upc.femfitai.controllers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upc.femfitai.entities.Ciclos;
import pe.edu.upc.femfitai.repositories.CiclosRepository;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/administracion/ciclos")
public class AdministracionCiclosController {

    private final CiclosRepository repository;

    public AdministracionCiclosController(CiclosRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<RegistroAdminDTO> listar(
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        if (pagina < 0 || tamano < 1 || tamano > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Parámetros de paginación inválidos"
            );
        }

        Pageable pageable = PageRequest.of(
                pagina,
                tamano,
                Sort.by(
                        Sort.Order.desc("fechaInicio"),
                        Sort.Order.desc("idCiclo")
                )
        );

        Page<Ciclos> ciclos;

        if (idUsuario == null) {
            ciclos = repository.findAll(pageable);
        } else {
            ciclos = repository.findByUsuario_IdUsuario(
                    idUsuario,
                    pageable
            );
        }

        return ciclos.map(this::convertir);
    }

    private RegistroAdminDTO convertir(Ciclos ciclo) {

        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("idCiclo", ciclo.getIdCiclo());
        datos.put("idUsuario", ciclo.getUsuario().getIdUsuario());
        datos.put("fechaInicio", ciclo.getFechaInicio());
        datos.put("fechaFinEstimada", ciclo.getFechaFinEstimada());
        datos.put("fechaReal", ciclo.getFechaReal());

        return new RegistroAdminDTO(
                ciclo.getUsuario().getIdUsuario(),
                datos,
                true
        );
    }

    public record RegistroAdminDTO(
            Integer idUsuario,
            Map<String, Object> datos,
            boolean modificable
    ) {}
}
