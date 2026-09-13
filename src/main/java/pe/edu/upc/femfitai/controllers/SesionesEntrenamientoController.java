package pe.edu.upc.femfitai.controllers;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTOInsert;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTOUpdate;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTOList;
import pe.edu.upc.femfitai.dtos.SesionesEntrenamientoDTODetalle;
import pe.edu.upc.femfitai.services.ISesionesEntrenamientoService;

@RestController
@RequestMapping("/sesiones")
public class SesionesEntrenamientoController {
    private final ISesionesEntrenamientoService service;

    public SesionesEntrenamientoController(ISesionesEntrenamientoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SesionesEntrenamientoDTOList> registrar(
            @RequestBody SesionesEntrenamientoDTOInsert datos) {
        SesionesEntrenamientoDTOList sesion = service.registrar(datos);
        return ResponseEntity.created(URI.create("/sesiones/" + sesion.idSesion())).body(sesion);
    }

    @GetMapping
    public List<SesionesEntrenamientoDTOList> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public SesionesEntrenamientoDTOList buscarPorId(@PathVariable("id") Integer id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public SesionesEntrenamientoDTOList actualizar(
            @PathVariable("id") Integer id, @RequestBody SesionesEntrenamientoDTOUpdate datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<SesionesEntrenamientoDTOList> listarPorUsuario(
            @PathVariable("idUsuario") Integer idUsuario) {
        return service.listarPorUsuario(idUsuario);
    }

    @GetMapping("/detalle/{id}")
    public SesionesEntrenamientoDTODetalle buscarDetallePorId(@PathVariable("id") Integer id) {
        return service.buscarDetallePorId(id);
    }
}
