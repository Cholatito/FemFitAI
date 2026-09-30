package pe.edu.upc.femfitai.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.femfitai.dtos.DetalleDiarioCicloDTO;
import pe.edu.upc.femfitai.dtos.DetalleDiarioCicloRequestDTO;
import pe.edu.upc.femfitai.services.interfaces.IDetalleDiarioCicloService;

import java.util.List;

@RestController
@RequestMapping("/detalle-diario-ciclo")
public class DetalleDiarioCicloController {

    private final IDetalleDiarioCicloService service;

    public DetalleDiarioCicloController(IDetalleDiarioCicloService service) {
        this.service = service;
    }

    @PostMapping
    public DetalleDiarioCicloDTO registrar(@RequestBody DetalleDiarioCicloDTO datos) {
        return service.registrar(datos);
    }

    @GetMapping
    public List<DetalleDiarioCicloDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public DetalleDiarioCicloDTO buscarPorId(@PathVariable("id") Integer id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public DetalleDiarioCicloDTO actualizar(@PathVariable("id") Integer id,
                                                       @RequestBody DetalleDiarioCicloDTO datos) {
        return service.actualizar(id, datos);
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
