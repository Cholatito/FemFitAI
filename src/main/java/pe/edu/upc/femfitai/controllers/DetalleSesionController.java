package pe.edu.upc.femfitai.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import pe.edu.upc.femfitai.dtos.DetalleSesionDTO;
import pe.edu.upc.femfitai.dtos.DetalleSesionEjercicioDTO;
import pe.edu.upc.femfitai.dtos.ObservacionSesionDTO;
import pe.edu.upc.femfitai.services.interfaces.IDetalleSesionService;

@RestController
@RequestMapping("/detalle-sesion")
@Tag(name = "DetalleSesion", description = "US49–US51")
@ApiResponse(responseCode = "400", description = "Datos invalidos")
@ApiResponse(responseCode = "401", description = "Autenticacion requerida")
@ApiResponse(responseCode = "403", description = "Recurso de otra cuenta")
@ApiResponse(responseCode = "404", description = "Recurso o referencia no encontrada")
@ApiResponse(responseCode = "409", description = "Conflicto de integridad")
public class DetalleSesionController {
    private final IDetalleSesionService service;
    public DetalleSesionController(IDetalleSesionService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "US49 Agregar ejercicio a sesion")
    @ResponseStatus(HttpStatus.CREATED)
    public DetalleSesionDTO registrar(@RequestBody DetalleSesionDTO d) {
        return service.registrar(d);
    }

    @GetMapping("/sesion/{idSesion}")
    @Operation(summary = "US49 Consultar ejercicios de sesion con JOIN")
    
    public List<DetalleSesionEjercicioDTO> listarPorSesion(@PathVariable("idSesion") Integer idSesion) {
        return service.listarPorSesion(idSesion);
    }

    @PutMapping("/{id}")
    @Operation(summary = "US50 Actualizar observacion")
    
    public DetalleSesionDTO actualizar(@PathVariable("id") Integer id, @RequestBody ObservacionSesionDTO d) {
        return service.actualizar(id, d);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "US51 Quitar ejercicio sin series dependientes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable("id") Integer id) {
        service.eliminar(id);
    }
}
