package pe.edu.upc.femfitai.controllers;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import pe.edu.upc.femfitai.dtos.ProgresoDTO;
import pe.edu.upc.femfitai.services.interfaces.IProgresoService;

@RestController
@RequestMapping("/progreso")
@Tag(name = "Progreso", description = "US12–US16")
@ApiResponse(responseCode = "400", description = "Datos invalidos")
@ApiResponse(responseCode = "401", description = "Autenticacion requerida")
@ApiResponse(responseCode = "403", description = "Recurso de otra cuenta")
@ApiResponse(responseCode = "404", description = "Recurso o referencia no encontrada")
@ApiResponse(responseCode = "409", description = "Conflicto de integridad")
public class ProgresoController {
    private final IProgresoService service;
    public ProgresoController(IProgresoService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "US12 US14 Registrar progreso y nota")
    @ResponseStatus(HttpStatus.CREATED)
    public ProgresoDTO registrar(@RequestBody ProgresoDTO d) {
        return service.registrar(d);
    }

    @GetMapping
    @Operation(summary = "US13 US15 US16 Historial paginado propio")
    
    public Page<ProgresoDTO> listar(@RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamano) {
        return service.listar(pagina, tamano);
    }

    @GetMapping("/{id}")
    @Operation(summary = "US16 Consultar un registro propio")
    
    public ProgresoDTO buscarPorId(@PathVariable("id") Integer id) {
        return service.buscarPorId(id);
    }
}
