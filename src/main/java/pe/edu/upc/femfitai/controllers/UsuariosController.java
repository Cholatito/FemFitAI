package pe.edu.upc.femfitai.controllers;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.femfitai.dtos.UsuariosDTOInsert;
import pe.edu.upc.femfitai.dtos.UsuariosDTOUpdate;
import pe.edu.upc.femfitai.dtos.UsuariosDTOList;
import pe.edu.upc.femfitai.services.IUsuariosService;

@RestController
@RequestMapping("/usuarios")
public class UsuariosController {
    private final IUsuariosService service;

    public UsuariosController(IUsuariosService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UsuariosDTOList> registrar(@RequestBody UsuariosDTOInsert datos) {
        UsuariosDTOList usuario = service.registrar(datos);
        return ResponseEntity.created(URI.create("/usuarios/" + usuario.idUsuario())).body(usuario);
    }

    @GetMapping
    public List<UsuariosDTOList> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public UsuariosDTOList buscarPorId(@PathVariable("id") Integer id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public UsuariosDTOList actualizar(@PathVariable("id") Integer id,
                                     @RequestBody UsuariosDTOUpdate datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}