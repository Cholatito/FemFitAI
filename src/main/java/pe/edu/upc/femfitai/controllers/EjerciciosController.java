package pe.edu.upc.femfitai.controllers;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.femfitai.dtos.EjerciciosDTO;
import pe.edu.upc.femfitai.dtos.EjerciciosUsuarioDTO;
import pe.edu.upc.femfitai.services.interfaces.IEjerciciosService;

@RestController
@RequestMapping("/ejercicios")
public class EjerciciosController {
    private final IEjerciciosService service;

    public EjerciciosController(IEjerciciosService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EjerciciosDTO> registrar(@RequestBody EjerciciosDTO datos) {
        EjerciciosDTO ejercicio = service.registrar(datos);
        return ResponseEntity.created(URI.create("/ejercicios/" + ejercicio.getIdEjercicio())).body(ejercicio);
    }

    @GetMapping
    public List<EjerciciosDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public EjerciciosDTO buscarPorId(@PathVariable("id") Integer id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public EjerciciosDTO actualizar(@PathVariable("id") Integer id,
                                    @RequestBody EjerciciosDTO datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/grupo-muscular/{grupoMuscular}")
    public List<EjerciciosDTO> buscarPorGrupoMuscular(
            @PathVariable("grupoMuscular") String grupoMuscular) {
        return service.buscarPorGrupoMuscular(grupoMuscular);
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<EjerciciosUsuarioDTO> listarPorUsuario(@PathVariable("idUsuario") Integer idUsuario) {
        return service.listarPorUsuario(idUsuario);
    }
}
