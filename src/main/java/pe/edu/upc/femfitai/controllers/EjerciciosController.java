package pe.edu.upc.femfitai.controllers;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.femfitai.dtos.EjerciciosDTOInsert;
import pe.edu.upc.femfitai.dtos.EjerciciosDTOList;
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
    public ResponseEntity<EjerciciosDTOList> registrar(@RequestBody EjerciciosDTOInsert datos) {
        EjerciciosDTOList ejercicio = service.registrar(datos);
        return ResponseEntity.created(URI.create("/ejercicios/" + ejercicio.idEjercicio())).body(ejercicio);
    }

    @GetMapping
    public List<EjerciciosDTOList> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public EjerciciosDTOList buscarPorId(@PathVariable("id") Integer id) {
        return service.buscarPorId(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/grupo-muscular/{grupoMuscular}")
    public List<EjerciciosDTOList> buscarPorGrupoMuscular(
            @PathVariable("grupoMuscular") String grupoMuscular) {
        return service.buscarPorGrupoMuscular(grupoMuscular);
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<EjerciciosUsuarioDTO> listarPorUsuario(@PathVariable("idUsuario") Integer idUsuario) {
        return service.listarPorUsuario(idUsuario);
    }
}
