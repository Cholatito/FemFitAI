package pe.edu.upc.femfitai.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.femfitai.dtos.*;
import pe.edu.upc.femfitai.services.interfaces.IAdministracionService;

@RestController
@RequestMapping("/administracion")
@PreAuthorize("hasRole('ADMIN')")
public class AdministracionController {
    private final IAdministracionService service;
    public AdministracionController(IAdministracionService service) {
        this.service = service;
    }

    @GetMapping("/{modulo}")
    public Page<RegistroAdministracionDTO> listar(@PathVariable String modulo,
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "10") int tamano) {
        return service.listar(modulo, idUsuario, pagina, tamano);
    }
    @GetMapping("/{modulo}/{id}")
    public RegistroAdministracionDTO buscar(@PathVariable String modulo, @PathVariable Long id) {
        return service.buscar(modulo, id);
    }
    @DeleteMapping("/{modulo}/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String modulo, @PathVariable Long id) {
        service.eliminar(modulo, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ciclos")
    public ResponseEntity<CiclosDTO> crearCiclo(@RequestBody CiclosDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardarCiclo(null, datos));
    }
    @PutMapping("/ciclos/{id}")
    public CiclosDTO actualizarCiclo(@PathVariable Long id, @RequestBody CiclosDTO datos) {
        return service.guardarCiclo(id, datos);
    }

    @PostMapping("/detalle-diario")
    public ResponseEntity<DetalleDiarioCicloDTO> crearDiario(@RequestBody DetalleDiarioCicloDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardarDiario(null, datos));
    }
    @PutMapping("/detalle-diario/{id}")
    public DetalleDiarioCicloDTO actualizarDiario(@PathVariable Integer id, @RequestBody DetalleDiarioCicloDTO datos) {
        return service.guardarDiario(id, datos);
    }

    @PostMapping("/rutinas")
    public ResponseEntity<RutinasDTO> crearRutina(@RequestBody RutinasDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardarRutina(null, datos));
    }
    @PutMapping("/rutinas/{id}")
    public RutinasDTO actualizarRutina(@PathVariable Integer id, @RequestBody RutinasDTO datos) {
        return service.guardarRutina(id, datos);
    }

    @PostMapping("/rutina-ejercicios")
    public ResponseEntity<RutinaEjerciciosDTO> crearAsignacion(@RequestBody RutinaEjerciciosDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardarAsignacion(null, datos));
    }
    @PutMapping("/rutina-ejercicios/{id}")
    public RutinaEjerciciosDTO actualizarAsignacion(@PathVariable Integer id, @RequestBody RutinaEjerciciosDTO datos) {
        return service.guardarAsignacion(id, datos);
    }

    @PostMapping("/sesiones")
    public ResponseEntity<SesionesEntrenamientoDTO> crearSesion(@RequestBody SesionesEntrenamientoDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardarSesion(null, datos));
    }
    @PutMapping("/sesiones/{id}")
    public SesionesEntrenamientoDTO actualizarSesion(@PathVariable Integer id, @RequestBody SesionesEntrenamientoDTO datos) {
        return service.guardarSesion(id, datos);
    }

    @PostMapping("/detalle-sesion")
    public ResponseEntity<DetalleSesionDTO> crearDetalle(@RequestBody DetalleSesionDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardarDetalle(null, datos));
    }
    @PutMapping("/detalle-sesion/{id}")
    public DetalleSesionDTO actualizarDetalle(@PathVariable Integer id, @RequestBody DetalleSesionDTO datos) {
        return service.guardarDetalle(id, datos);
    }

    @PostMapping("/detalle-serie")
    public ResponseEntity<DetalleSerieDTO> crearSerie(@RequestBody DetalleSerieDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardarSerie(null, datos));
    }
    @PutMapping("/detalle-serie/{id}")
    public DetalleSerieDTO actualizarSerie(@PathVariable Integer id, @RequestBody DetalleSerieDTO datos) {
        return service.guardarSerie(id, datos);
    }

    @PostMapping("/progreso")
    public ResponseEntity<ProgresoDTO> crearProgreso(@RequestBody ProgresoDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardarProgreso(null, datos));
    }
    @PutMapping("/progreso/{id}")
    public ProgresoDTO actualizarProgreso(@PathVariable Integer id, @RequestBody ProgresoDTO datos) {
        return service.guardarProgreso(id, datos);
    }

    @PostMapping("/perfiles")
    public ResponseEntity<PerfilEntrenamientoDTO> crearPerfil(@RequestBody PerfilEntrenamientoDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardarPerfil(null, datos));
    }
    @PutMapping("/perfiles/{id}")
    public PerfilEntrenamientoDTO actualizarPerfil(@PathVariable Integer id, @RequestBody PerfilEntrenamientoDTO datos) {
        return service.guardarPerfil(id, datos);
    }

    @PostMapping("/recomendaciones")
    public ResponseEntity<RecomendacionesIADTO> crearRecomendacion(@RequestBody RecomendacionesIADTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardarRecomendacion(null, datos));
    }
    @PutMapping("/recomendaciones/{id}")
    public RecomendacionesIADTO actualizarRecomendacion(@PathVariable Integer id, @RequestBody RecomendacionesIADTO datos) {
        return service.guardarRecomendacion(id, datos);
    }

    // El handler global del proyecto captura Exception; aquí se conserva el 403 específico.
    @ExceptionHandler(AccessDeniedException.class)
    @PreAuthorize("permitAll()")
    public ResponseEntity<ErrorResponse> accesoDenegado(AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(
                403, "Solo ADMIN puede administrar estos registros", request.getRequestURI()));
    }
}
