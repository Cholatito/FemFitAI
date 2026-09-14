package pe.edu.upc.femfitai.controllers;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.femfitai.dtos.RutinasDTOInsert;
import pe.edu.upc.femfitai.dtos.RutinasDTOUpdate;
import pe.edu.upc.femfitai.dtos.RutinasDTOList;
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
    public ResponseEntity<RutinasDTOList> registrar(@RequestBody RutinasDTOInsert datos) {
        RutinasDTOList rutina = service.registrar(datos);
        return ResponseEntity.created(URI.create("/rutinas/" + rutina.idRutina())).body(rutina);
    }

    @GetMapping
    public List<RutinasDTOList> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public RutinasDTOList buscarPorId(@PathVariable("id") Integer id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public RutinasDTOList actualizar(@PathVariable("id") Integer id,
                                    @RequestBody RutinasDTOUpdate datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<RutinasDTOList> listarPorUsuario(@PathVariable("idUsuario") Integer idUsuario) {
        return service.listarPorUsuario(idUsuario);
    }

    @GetMapping("/detalle/{id}")
    public RutinasUsuarioDTO buscarDetallePorId(@PathVariable("id") Integer id) {
        return service.buscarDetallePorId(id);
    }
}