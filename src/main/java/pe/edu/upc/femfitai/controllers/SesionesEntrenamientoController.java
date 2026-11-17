package pe.edu.upc.femfitai.controllers;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
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
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                "Las sesiones nuevas deben iniciarse desde una rutina planificada con POST /sesiones/rutina/{idRutina}/iniciar");
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
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                "La sesion no puede modificarse mediante este endpoint. Use PUT /sesiones/{id}/finalizar para cerrar una sesion EN_CURSO");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                "La sesion no puede eliminarse por historial. Mantenga el registro de entrenamiento");
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