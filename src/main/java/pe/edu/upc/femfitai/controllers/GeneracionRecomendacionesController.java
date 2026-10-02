package pe.edu.upc.femfitai.controllers;

import java.net.URI;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.femfitai.dtos.GenerarRecomendacionDTO;
import pe.edu.upc.femfitai.dtos.RecomendacionesIADTO;
import pe.edu.upc.femfitai.services.implementations.GeneracionRecomendacionesService;

@RestController
@RequestMapping("/recomendaciones")
public class GeneracionRecomendacionesController {
    private final GeneracionRecomendacionesService service;

    public GeneracionRecomendacionesController(GeneracionRecomendacionesService service) {
        this.service = service;
    }

    @PostMapping("/generar")
    @Operation(summary = "US17/US20 Integracion preparada; responde 503 hasta conectar el generador real")
    public ResponseEntity<RecomendacionesIADTO> generar(@RequestBody GenerarRecomendacionDTO datos) {
        var resultado = service.generar(datos);
        return ResponseEntity.created(URI.create("/recomendaciones/" + resultado.idRecomendacion())).body(resultado);
    }
}
