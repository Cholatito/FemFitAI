package pe.edu.upc.femfitai.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.femfitai.dtos.RecomendacionesIADTO;
import pe.edu.upc.femfitai.services.interfaces.IRecomendacionesIAService;

@RestController
@RequestMapping("/recomendaciones")
@Tag(name = "RecomendacionesIA", description = "Consulta y aceptacion; generacion pendiente de reglas funcionales")
public class RecomendacionesIAController {
    private final IRecomendacionesIAService service;

    public RecomendacionesIAController(IRecomendacionesIAService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "US18 Consultar recomendaciones propias")
    public Page<RecomendacionesIADTO> listar(@RequestParam(defaultValue = "0") int pagina,
                                           @RequestParam(defaultValue = "20") int tamano) {
        return service.listar(pagina, tamano);
    }

    @GetMapping("/{id}")
    @Operation(summary = "US18 Consultar una recomendacion propia")
    public RecomendacionesIADTO buscarPorId(@PathVariable Integer id) {
        return service.buscarPorId(id);
    }

    @PatchMapping("/{id}/aceptar")
    @Operation(summary = "US19 Aceptar una recomendacion propia")
    public RecomendacionesIADTO aceptar(@PathVariable Integer id) {
        return service.aceptar(id);
    }
}
