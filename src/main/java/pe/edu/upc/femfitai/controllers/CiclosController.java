package pe.edu.upc.femfitai.controllers;

import pe.edu.upc.femfitai.dtos.CiclosUsuarioDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import pe.edu.upc.femfitai.dtos.CiclosDTOInsert;
import pe.edu.upc.femfitai.dtos.CiclosDTO;
import pe.edu.upc.femfitai.dtos.CiclosDTOUpdate;
import pe.edu.upc.femfitai.services.interfaces.ICiclosService;

@RestController
@RequestMapping("/ciclos")
public class CiclosController {

    private final ICiclosService service;

    public CiclosController(ICiclosService service) {
        this.service = service;
    }

    @PostMapping
    public CiclosDTO registrar(@RequestBody CiclosDTO datos) {
        return service.registrar(datos);
    }

    @GetMapping
    public List<CiclosDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public CiclosDTO buscarPorId(@PathVariable("id") Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public CiclosDTO actualizar(@PathVariable("id") Long id,
                                @RequestBody CiclosDTOUpdate datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<CiclosDTO> listarPorUsuario(@PathVariable("idUsuario") Long idUsuario) {
        return service.listarPorUsuario(idUsuario);
    }

    @GetMapping("/detalle/{id}")
    public CiclosUsuarioDTO buscarDetallePorId(
            @PathVariable("id") Long id) {
        return service.buscarDetallePorId(id);
    }
}
