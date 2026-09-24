package pe.edu.upc.femfitai.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import pe.edu.upc.femfitai.dtos.RutinaEjercicioDetalleDTO;
import pe.edu.upc.femfitai.dtos.RutinaEjerciciosDTO;
import pe.edu.upc.femfitai.services.interfaces.IRutinaEjerciciosService;

@RestController
@RequestMapping("/rutina-ejercicios")
@Tag(name = "RutinaEjercicios", description = "US22–US26")
@ApiResponse(responseCode = "400", description = "Datos invalidos")
@ApiResponse(responseCode = "401", description = "Autenticacion requerida")
@ApiResponse(responseCode = "403", description = "Recurso de otra cuenta")
@ApiResponse(responseCode = "404", description = "Recurso o referencia no encontrada")
@ApiResponse(responseCode = "409", description = "Conflicto de integridad")
public class RutinaEjerciciosController {
    private final IRutinaEjerciciosService service;
    public RutinaEjerciciosController(IRutinaEjerciciosService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "US22 Agregar ejercicio a rutina")
    @ResponseStatus(HttpStatus.CREATED)
    public RutinaEjerciciosDTO registrar(@RequestBody RutinaEjerciciosDTO d) {
        return service.registrar(d);
    }

    @PutMapping("/{id}")
    @Operation(summary = "US23 US24 Configurar series repeticiones y descanso")
    
    public RutinaEjerciciosDTO actualizar(@PathVariable("id") Integer id, @RequestBody RutinaEjerciciosDTO d) {
        return service.actualizar(id, d);
    }

    @GetMapping("/rutina/{idRutina}")
    @Operation(summary = "US25 US26 Consultar ejercicios de rutina con JOIN")
    
    public List<RutinaEjercicioDetalleDTO> listarPorRutina(@PathVariable("idRutina") Integer idRutina) {
        return service.listarPorRutina(idRutina);
    }
}
