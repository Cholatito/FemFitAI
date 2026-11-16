package pe.edu.upc.femfitai.controllers;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.femfitai.dtos.EjerciciosDTO;
import pe.edu.upc.femfitai.dtos.EjerciciosUsuarioDTO;
import pe.edu.upc.femfitai.services.interfaces.IEjerciciosService;
import pe.edu.upc.femfitai.services.interfaces.IWgerSyncService;

@RestController
@RequestMapping("/ejercicios")
public class EjerciciosController {
    private final IEjerciciosService service;
    private final IWgerSyncService wgerSyncService;

    @GetMapping("/buscar")
    @io.swagger.v3.oas.annotations.Operation(summary = "US34 Buscar ejercicios por tipo y nombre con paginacion")
    public org.springframework.data.domain.Page<EjerciciosDTO> buscar(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        return service.buscar(new pe.edu.upc.femfitai.dtos.BusquedaEjerciciosDTO(tipo, query, pagina, tamano));
    }

    public EjerciciosController(IEjerciciosService service, IWgerSyncService wgerSyncService) {
        this.service = service;
        this.wgerSyncService = wgerSyncService;
    }

    @PostMapping("/wger/sincronizar")
    public ResponseEntity<IWgerSyncService.ResultadoSincronizacion> sincronizarWger() {
        IWgerSyncService.ResultadoSincronizacion resumen = wgerSyncService.sincronizarTodos();
        return resumen.completo()
                ? ResponseEntity.ok(resumen)
                : ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(resumen);
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
