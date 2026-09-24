package pe.edu.upc.femfitai.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import pe.edu.upc.femfitai.services.interfaces.IDetalleSerieService;

@RestController
@RequestMapping("/detalle-serie")
@Tag(name = "DetalleSerie", description = "US44–US46")
@ApiResponse(responseCode = "400", description = "Datos invalidos")
@ApiResponse(responseCode = "401", description = "Autenticacion requerida")
@ApiResponse(responseCode = "403", description = "Recurso de otra cuenta")
@ApiResponse(responseCode = "404", description = "Recurso o referencia no encontrada")
@ApiResponse(responseCode = "409", description = "Conflicto de integridad")
public class DetalleSerieController {
    private final IDetalleSerieService service;
    public DetalleSerieController(IDetalleSerieService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "US44 Registrar serie individual")
    @ResponseStatus(HttpStatus.CREATED)
    public DetalleSerieDTO registrar(@RequestBody DetalleSerieDTO d) {
        return service.registrar(d);
    }

    @GetMapping("/detalle/{idDetalle}")
    @Operation(summary = "US44 Consultar series de un ejercicio")
    
    public List<DetalleSerieDTO> listarPorDetalle(@PathVariable("idDetalle") Integer idDetalle) {
        return service.listarPorDetalle(idDetalle);
    }

    @PutMapping("/{id}")
    @Operation(summary = "US45 Editar peso y repeticiones")
    
    public DetalleSerieDTO actualizar(@PathVariable("id") Integer id, @RequestBody ActualizarSerieDTO d) {
        return service.actualizar(id, d);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "US46 Eliminar serie")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable("id") Integer id) {
        service.eliminar(id);
    }
}
