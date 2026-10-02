package pe.edu.upc.femfitai.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.femfitai.dtos.DetalleDiarioCicloDTO;
import pe.edu.upc.femfitai.services.interfaces.IDetalleDiarioCicloService;

@RestController
@RequestMapping("/detalle-diario")
public class DetalleDiarioCicloController {
    private final IDetalleDiarioCicloService service;

    public DetalleDiarioCicloController(IDetalleDiarioCicloService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DetalleDiarioCicloDTO> guardar(@RequestBody DetalleDiarioCicloDTO datos) {
        DetalleDiarioCicloDTO r = service.guardar(datos);
        return ResponseEntity.created(URI.create("/detalle-diario/" + r.getIdDetalleDiarioCiclo())).body(r);
    }

    @PutMapping
    public ResponseEntity<DetalleDiarioCicloDTO> guardarOActualizar(@RequestBody DetalleDiarioCicloDTO datos) {
        return ResponseEntity.ok(service.guardar(datos));
    }
    @GetMapping
    public List<DetalleDiarioCicloDTO> listar() {
        return service.listar();
    }
    @GetMapping("/{id}")
    public DetalleDiarioCicloDTO buscarPorId(@PathVariable("id") Integer id) {
        return service.buscarPorId(id);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/ciclo/{idCiclo}")
    public List<DetalleDiarioCicloDTO> listarPorCiclo(@PathVariable("idCiclo") Long idCiclo) {
        return service.listarPorCiclo(idCiclo);
    }
}
