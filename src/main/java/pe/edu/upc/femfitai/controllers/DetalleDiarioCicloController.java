package pe.edu.upc.femfitai.controllers;

import java.net.URI;
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
        return ResponseEntity.created(URI.create("/detalle-diario/" + r.idDetalleDiarioCiclo())).body(r);
    }

    @PutMapping
    public ResponseEntity<DetalleDiarioCicloDTO> guardarOActualizar(@RequestBody DetalleDiarioCicloDTO datos) {
        return ResponseEntity.ok(service.guardar(datos));
    }
}
