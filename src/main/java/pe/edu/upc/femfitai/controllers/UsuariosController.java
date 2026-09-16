package pe.edu.upc.femfitai.controllers;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.femfitai.dtos.UsuariosDTO;
import pe.edu.upc.femfitai.services.interfaces.IUsuariosService;

@RestController
@RequestMapping("/usuarios")
public class UsuariosController {
    private final IUsuariosService service;

    public UsuariosController(IUsuariosService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UsuariosDTO> registrar(@RequestBody UsuariosDTO datos) {
        UsuariosDTO usuario = service.registrar(datos);
        return ResponseEntity.created(URI.create("/usuarios/" + usuario.getIdUsuario())).body(usuario);
    }

    @GetMapping
    public List<UsuariosDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public UsuariosDTO buscarPorId(@PathVariable("id") Integer id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public UsuariosDTO actualizar(@PathVariable("id") Integer id,
                                     @RequestBody UsuariosDTO datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}