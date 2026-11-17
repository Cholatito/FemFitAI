package pe.edu.upc.femfitai.controllers;

import java.net.URI;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.femfitai.dtos.GenerarRecomendacionDTO;
import pe.edu.upc.femfitai.dtos.RecomendacionesIADTO;
import pe.edu.upc.femfitai.services.implementations.GeneracionRecomendacionesService;
import pe.edu.upc.femfitai.dtos.GenerarRecomendacionCicloDTO;
import pe.edu.upc.femfitai.services.implementations.RecomendacionCicloService;
@RestController
@RequestMapping("/recomendaciones")
public class GeneracionRecomendacionesController {
    private final GeneracionRecomendacionesService service;
    private final RecomendacionCicloService cicloService;
    public GeneracionRecomendacionesController(
            GeneracionRecomendacionesService service,
            RecomendacionCicloService cicloService) {

        this.service = service;
        this.cicloService = cicloService;
    }

    @PostMapping("/generar")
    @Operation(summary = "US17/US20 Integracion preparada; responde 503 hasta conectar el generador real")
    public ResponseEntity<RecomendacionesIADTO> generar(@RequestBody GenerarRecomendacionDTO datos) {
        var resultado = service.generar(datos);
        return ResponseEntity.created(URI.create("/recomendaciones/" + resultado.idRecomendacion())).body(resultado);
    }

    @PostMapping("/ciclo")
    @Operation(summary = "Generar recomendación de entrenamiento según el ciclo menstrual")
    public ResponseEntity<RecomendacionesIADTO> generarPorCiclo(
            @RequestBody GenerarRecomendacionCicloDTO datos) {

        RecomendacionesIADTO resultado =
                cicloService.generar(datos);

        return ResponseEntity
                .created(URI.create(
                        "/recomendaciones/" + resultado.idRecomendacion()))
                .body(resultado);
    }
}
