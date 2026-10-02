package pe.edu.upc.femfitai.controllers;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.femfitai.dtos.RutinasDTO;
import pe.edu.upc.femfitai.dtos.RutinasUsuarioDTO;
import pe.edu.upc.femfitai.services.interfaces.IRutinasService;

@RestController
@RequestMapping("/rutinas")
public class RutinasController {
    private final IRutinasService service;

    public RutinasController(IRutinasService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RutinasDTO> registrar(@RequestBody RutinasDTO datos) {
        RutinasDTO rutina = service.registrar(datos);
        return ResponseEntity.created(URI.create("/rutinas/" + rutina.getIdRutina())).body(rutina);
    }

    @GetMapping
    public List<RutinasDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public RutinasDTO buscarPorId(@PathVariable("id") Integer id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public RutinasDTO actualizar(@PathVariable("id") Integer id,
                                    @RequestBody RutinasDTO datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<RutinasDTO> listarPorUsuario(@PathVariable("idUsuario") Integer idUsuario) {
        return service.listarPorUsuario(idUsuario);
    }

    @GetMapping("/detalle/{id}")
    public RutinasUsuarioDTO buscarDetallePorId(@PathVariable("id") Integer id) {
        return service.buscarDetallePorId(id);
    }
}