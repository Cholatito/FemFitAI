package pe.edu.upc.femfitai.controllers;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.femfitai.dtos.FinalizarSesionDTO;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTO;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTODetalle;
import pe.edu.upc.femfitai.services.interfaces.ISesionesEntrenamientoService;

@RestController
@RequestMapping("/sesiones")
public class SesionesEntrenamientoController {
    private final ISesionesEntrenamientoService service;

    public SesionesEntrenamientoController(ISesionesEntrenamientoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SesionesEntrenamientoDTO> registrar(
            @RequestBody SesionesEntrenamientoDTO datos) {
        SesionesEntrenamientoDTO sesion = service.registrar(datos);
        return ResponseEntity.created(URI.create("/sesiones/" + sesion.getIdSesion())).body(sesion);
    }

    @PostMapping("/rutina/{idRutina}/iniciar")
    public ResponseEntity<SesionesEntrenamientoDTO> iniciarDesdeRutina(
            @PathVariable("idRutina") Integer idRutina) {
        SesionesEntrenamientoDTO sesion = service.iniciarDesdeRutina(idRutina);
        return ResponseEntity.created(URI.create("/sesiones/" + sesion.getIdSesion())).body(sesion);
    }

    @PutMapping("/{id}/finalizar")
    public SesionesEntrenamientoDTO finalizar(
            @PathVariable("id") Integer id,
            @RequestBody FinalizarSesionDTO datos) {
        return service.finalizar(id, datos);
    }

    @GetMapping
    public List<SesionesEntrenamientoDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public SesionesEntrenamientoDTO buscarPorId(@PathVariable("id") Integer id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public SesionesEntrenamientoDTO actualizar(
            @PathVariable("id") Integer id, @RequestBody SesionesEntrenamientoDTO datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<SesionesEntrenamientoDTO> listarPorUsuario(
            @PathVariable("idUsuario") Integer idUsuario) {
        return service.listarPorUsuario(idUsuario);
    }

    @GetMapping("/detalle/{id}")
    public SesionesEntrenamientoDTODetalle buscarDetallePorId(@PathVariable("id") Integer id) {
        return service.buscarDetallePorId(id);
    }
}