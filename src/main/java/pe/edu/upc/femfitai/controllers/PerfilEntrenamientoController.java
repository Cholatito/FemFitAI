package pe.edu.upc.femfitai.controllers;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import pe.edu.upc.femfitai.dtos.*;
import pe.edu.upc.femfitai.services.interfaces.IPerfilEntrenamientoService;

@RestController
@RequestMapping("/perfiles")
@Tag(name = "PerfilEntrenamiento", description = "US37–US41")
@ApiResponse(responseCode = "400", description = "Datos invalidos")
@ApiResponse(responseCode = "401", description = "Autenticacion requerida")
@ApiResponse(responseCode = "403", description = "Recurso de otra cuenta")
@ApiResponse(responseCode = "404", description = "Recurso o referencia no encontrada")
@ApiResponse(responseCode = "409", description = "Conflicto de integridad")
public class PerfilEntrenamientoController {
    private final IPerfilEntrenamientoService service;
    public PerfilEntrenamientoController(IPerfilEntrenamientoService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "US37 Registrar perfil propio")
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilEntrenamientoDTO registrar(@RequestBody PerfilEntrenamientoDTO d) {
        return service.registrar(d);
    }

    @GetMapping
    @Operation(summary = "US38 Consultar perfil propio")

    public PerfilEntrenamientoDTO consultar() {
        return service.consultar();
    }

    @PutMapping
    @Operation(summary = "US39 Actualizar nivel y objetivo")

    public PerfilEntrenamientoDTO actualizar(@RequestBody ActualizarPerfilDTO d) {
        return service.actualizar(d);
    }

    @PatchMapping("/disponibilidad")
    @Operation(summary = "US40 Actualizar dias y tiempo")

    public PerfilEntrenamientoDTO disponibilidad(@RequestBody DisponibilidadDTO d) {
        return service.disponibilidad(d);
    }
}
