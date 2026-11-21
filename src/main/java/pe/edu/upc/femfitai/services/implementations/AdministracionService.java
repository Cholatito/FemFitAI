package pe.edu.upc.femfitai.services.implementations;

import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.femfitai.dtos.*;
import pe.edu.upc.femfitai.entities.*;
import pe.edu.upc.femfitai.repositories.*;
import pe.edu.upc.femfitai.services.interfaces.IAdministracionService;
import static pe.edu.upc.femfitai.services.implementations.Validaciones.*;

@Service
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public class AdministracionService implements IAdministracionService {
    private final CiclosRepository rCiclo;
    private final DetalleDiarioCicloRepository rDiario;
    private final RutinasRepository rRutina;
    private final RutinaEjerciciosRepository rAsignacion;
    private final SesionesEntrenamientoRepository rSesion;
    private final DetalleSesionRepository rDetalle;
    private final DetalleSerieRepository rSerie;
    private final ProgresoRepository rProgreso;
    private final PerfilEntrenamientoRepository rPerfil;
    private final RecomendacionesIARepository rRecomendacion;
    private final UsuariosRepository usuarios;
    private final EjerciciosRepository ejercicios;
    public AdministracionService(CiclosRepository rCiclo,
            DetalleDiarioCicloRepository rDiario,
            RutinasRepository rRutina,
            RutinaEjerciciosRepository rAsignacion,
            SesionesEntrenamientoRepository rSesion,
            DetalleSesionRepository rDetalle,
            DetalleSerieRepository rSerie,
            ProgresoRepository rProgreso,
            PerfilEntrenamientoRepository rPerfil,
            RecomendacionesIARepository rRecomendacion,
            UsuariosRepository usuarios, EjerciciosRepository ejercicios) {
        this.rCiclo = rCiclo;
        this.rDiario = rDiario;
        this.rRutina = rRutina;
        this.rAsignacion = rAsignacion;
        this.rSesion = rSesion;
        this.rDetalle = rDetalle;
        this.rSerie = rSerie;
        this.rProgreso = rProgreso;
        this.rPerfil = rPerfil;
        this.rRecomendacion = rRecomendacion;
        this.usuarios = usuarios;
        this.ejercicios = ejercicios;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegistroAdministracionDTO> listar(String modulo, Integer idUsuario, int pagina, int tamano) {
        paginar(pagina,tamano);
        if (idUsuario != null) positivo(idUsuario,"IdUsuario");
        return switch(modulo) {
            case "ciclos" -> (idUsuario == null ? rCiclo.findAll(PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idCiclo"))) : rCiclo.findByUsuario_IdUsuario(idUsuario.longValue(),PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idCiclo")))).map(e -> new RegistroAdministracionDTO(e.getUsuario().getIdUsuario(), dto(e)));
            case "detalle-diario" -> (idUsuario == null ? rDiario.findAll(PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idDetalleDiarioCiclo"))) : rDiario.findByCiclo_Usuario_IdUsuario(idUsuario,PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idDetalleDiarioCiclo")))).map(e -> new RegistroAdministracionDTO(e.getCiclo().getUsuario().getIdUsuario(), dto(e)));
            case "rutinas" -> (idUsuario == null ? rRutina.findAll(PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idRutina"))) : rRutina.findByUsuario_IdUsuario(idUsuario,PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idRutina")))).map(e -> new RegistroAdministracionDTO(e.getUsuario().getIdUsuario(), dto(e)));
            case "rutina-ejercicios" -> (idUsuario == null ? rAsignacion.findAll(PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idRutinaEjercicio"))) : rAsignacion.findByRutina_Usuario_IdUsuario(idUsuario,PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idRutinaEjercicio")))).map(e -> new RegistroAdministracionDTO(e.getRutina().getUsuario().getIdUsuario(), dto(e)));
            case "sesiones" -> (idUsuario == null ? rSesion.findAll(PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idSesion"))) : rSesion.findByUsuario_IdUsuario(idUsuario,PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idSesion")))).map(e -> new RegistroAdministracionDTO(e.getUsuario().getIdUsuario(), dto(e), "EN_CURSO".equals(e.getEstado())));
            case "detalle-sesion" -> (idUsuario == null ? rDetalle.findAll(PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idDetalle"))) : rDetalle.findBySesion_Usuario_IdUsuario(idUsuario,PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idDetalle")))).map(e -> new RegistroAdministracionDTO(e.getSesion().getUsuario().getIdUsuario(), dto(e), "EN_CURSO".equals(e.getSesion().getEstado())));
            case "detalle-serie" -> (idUsuario == null ? rSerie.findAll(PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idSerie"))) : rSerie.findByDetalle_Sesion_Usuario_IdUsuario(idUsuario,PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idSerie")))).map(e -> new RegistroAdministracionDTO(e.getDetalle().getSesion().getUsuario().getIdUsuario(), dto(e), "EN_CURSO".equals(e.getDetalle().getSesion().getEstado())));
            case "progreso" -> (idUsuario == null ? rProgreso.findAll(PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idProgreso"))) : rProgreso.findByUsuario_IdUsuario(idUsuario,PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idProgreso")))).map(e -> new RegistroAdministracionDTO(e.getUsuario().getIdUsuario(), dto(e)));
            case "perfiles" -> (idUsuario == null ? rPerfil.findAll(PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idPerfil"))) : rPerfil.findByUsuario_IdUsuario(idUsuario,PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idPerfil")))).map(e -> new RegistroAdministracionDTO(e.getUsuario().getIdUsuario(), dto(e)));
            case "recomendaciones" -> (idUsuario == null ? rRecomendacion.findAll(PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idRecomendacion"))) : rRecomendacion.findByUsuario_IdUsuario(idUsuario,PageRequest.of(pagina,tamano,Sort.by(Sort.Direction.DESC,"idRecomendacion")))).map(e -> new RegistroAdministracionDTO(e.getUsuario().getIdUsuario(), dto(e)));
            default -> throw noEncontrado("Módulo administrativo");
        };
    }
    @Override
    @Transactional(readOnly = true)
    public RegistroAdministracionDTO buscar(String modulo, Long id) {
        exigir(id != null && id > 0 && id <= Integer.MAX_VALUE,"Identificador inválido");
        return switch(modulo) {
            case "ciclos" -> { var e = rCiclo.findById(id).orElseThrow(() -> noEncontrado("Registro")); yield new RegistroAdministracionDTO(e.getUsuario().getIdUsuario(), dto(e)); }
            case "detalle-diario" -> { var e = rDiario.findById(id.intValue()).orElseThrow(() -> noEncontrado("Registro")); yield new RegistroAdministracionDTO(e.getCiclo().getUsuario().getIdUsuario(), dto(e)); }
            case "rutinas" -> { var e = rRutina.findById(id.intValue()).orElseThrow(() -> noEncontrado("Registro")); yield new RegistroAdministracionDTO(e.getUsuario().getIdUsuario(), dto(e)); }
            case "rutina-ejercicios" -> { var e = rAsignacion.findById(id.intValue()).orElseThrow(() -> noEncontrado("Registro")); yield new RegistroAdministracionDTO(e.getRutina().getUsuario().getIdUsuario(), dto(e)); }
            case "sesiones" -> { var e = rSesion.findById(id.intValue()).orElseThrow(() -> noEncontrado("Registro")); yield new RegistroAdministracionDTO(e.getUsuario().getIdUsuario(), dto(e), "EN_CURSO".equals(e.getEstado())); }
            case "detalle-sesion" -> { var e = rDetalle.findById(id.intValue()).orElseThrow(() -> noEncontrado("Registro")); yield new RegistroAdministracionDTO(e.getSesion().getUsuario().getIdUsuario(), dto(e), "EN_CURSO".equals(e.getSesion().getEstado())); }
            case "detalle-serie" -> { var e = rSerie.findById(id.intValue()).orElseThrow(() -> noEncontrado("Registro")); yield new RegistroAdministracionDTO(e.getDetalle().getSesion().getUsuario().getIdUsuario(), dto(e), "EN_CURSO".equals(e.getDetalle().getSesion().getEstado())); }
            case "progreso" -> { var e = rProgreso.findById(id.intValue()).orElseThrow(() -> noEncontrado("Registro")); yield new RegistroAdministracionDTO(e.getUsuario().getIdUsuario(), dto(e)); }
            case "perfiles" -> { var e = rPerfil.findById(id.intValue()).orElseThrow(() -> noEncontrado("Registro")); yield new RegistroAdministracionDTO(e.getUsuario().getIdUsuario(), dto(e)); }
            case "recomendaciones" -> { var e = rRecomendacion.findById(id.intValue()).orElseThrow(() -> noEncontrado("Registro")); yield new RegistroAdministracionDTO(e.getUsuario().getIdUsuario(), dto(e)); }
            default -> throw noEncontrado("Módulo administrativo");
        };
    }
    @Override
    public void eliminar(String modulo, Long id) {
        buscar(modulo,id);
        switch(modulo) {
            case "ciclos" -> { conflicto(rDiario.existsByCiclo_IdCiclo(id), "El ciclo tiene registros diarios; elimínelos primero"); rCiclo.deleteById(id); rCiclo.flush(); }
            case "detalle-diario" -> { rDiario.deleteById(id.intValue()); rDiario.flush(); }
            case "rutinas" -> { conflicto(!rAsignacion.findByRutina_IdRutinaOrderByIdRutinaEjercicioAsc(id.intValue()).isEmpty() || rSesion.existsByRutina_IdRutina(id.intValue()) || rRecomendacion.existsByRutina_IdRutina(id.intValue()), "La rutina tiene ejercicios, sesiones o recomendaciones relacionadas"); rRutina.deleteById(id.intValue()); rRutina.flush(); }
            case "rutina-ejercicios" -> { rAsignacion.deleteById(id.intValue()); rAsignacion.flush(); }
            case "sesiones" -> throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Las sesiones se conservan como historial y no se eliminan");
            case "detalle-sesion" -> { var detalle=rDetalle.findById(id.intValue()).orElseThrow(() -> noEncontrado("Detalle")); enCurso(detalle.getSesion()); conflicto(rSerie.existsByDetalle_IdDetalle(id.intValue()), "El ejercicio tiene series; elimínelas primero"); rDetalle.deleteById(id.intValue()); rDetalle.flush(); }
            case "detalle-serie" -> { enCurso(rSerie.findById(id.intValue()).orElseThrow(() -> noEncontrado("Serie")).getDetalle().getSesion()); rSerie.deleteById(id.intValue()); rSerie.flush(); }
            case "progreso" -> { rProgreso.deleteById(id.intValue()); rProgreso.flush(); }
            case "perfiles" -> { rPerfil.deleteById(id.intValue()); rPerfil.flush(); }
            case "recomendaciones" -> { rRecomendacion.deleteById(id.intValue()); rRecomendacion.flush(); }
            default -> throw noEncontrado("Módulo administrativo");
        }
    }
    private Usuarios usuario(Integer id) {
        positivo(id,"IdUsuario");
        return usuarios.findById(id).orElseThrow(() -> noEncontrado("Usuario"));
    }
    private void identificador(Number id, Number recibido) {
        if (id != null) exigir(id.longValue()>0 && id.longValue()<=Integer.MAX_VALUE,"Identificador inválido");
        if (recibido != null && recibido.longValue()!=0)
            exigir(id != null && recibido.longValue()==id.longValue(),"El ID del cuerpo no coincide con la ruta");
    }
    private void referencia(Object actual, Object recibida) {
        exigir(Objects.equals(actual,recibida),"No se puede cambiar la pertenencia ni las referencias de un registro existente");
    }
    private void energia(Integer valor, int maximo, String campo) {
        exigir(valor != null && valor>=1 && valor<=maximo,campo+" debe estar entre 1 y "+maximo);
    }
    private void noFutura(LocalDate fecha, String campo) {
        exigir(fecha != null && !fecha.isAfter(LocalDate.now()),campo+" obligatoria y no futura");
    }
    private void enCurso(SesionesEntrenamiento sesion) {
        conflicto(!"EN_CURSO".equals(sesion.getEstado()),"La sesión ya no está en curso; se conserva su historial");
    }

    @Override
    public CiclosDTO guardarCiclo(Long id, CiclosDTO d) {
        exigir(d!=null,"Los datos son obligatorios"); identificador(id,d.getIdCiclo());
        exigir(d.getIdUsuario()!=null && d.getIdUsuario()>0 && d.getIdUsuario()<=Integer.MAX_VALUE,"IdUsuario inválido");
        var u=usuario(d.getIdUsuario().intValue());
        var e=id==null?new Ciclos():rCiclo.findById(id).orElseThrow(() -> noEncontrado("Ciclo"));
        if(id!=null) referencia(e.getUsuario().getIdUsuario(),u.getIdUsuario());
        noFutura(d.getFechaInicio(),"FechaInicio");
        exigir(d.getFechaReal()==null || !d.getFechaReal().isBefore(d.getFechaInicio()),"FechaFinReal no puede ser anterior al inicio");
        usuarios.bloquear(u.getIdUsuario()).orElseThrow(() -> noEncontrado("Usuario"));
        if(d.getFechaReal()==null) conflicto(id==null?rCiclo.existsByUsuario_IdUsuarioAndFechaRealIsNull(d.getIdUsuario()):rCiclo.existsByUsuario_IdUsuarioAndFechaRealIsNullAndIdCicloNot(d.getIdUsuario(),id),"La usuaria ya tiene otro ciclo activo");
        e.setUsuario(u);e.setFechaInicio(d.getFechaInicio());e.setFechaFinEstimada(d.getFechaInicio().plusDays(28));e.setFechaReal(d.getFechaReal());
        return dto(rCiclo.saveAndFlush(e));
    }
    @Override
    public DetalleDiarioCicloDTO guardarDiario(Integer id, DetalleDiarioCicloDTO d) {
        exigir(d!=null,"Los datos son obligatorios");identificador(id,d.getIdDetalleDiarioCiclo());
        exigir(d.getIdCiclo()!=null && d.getIdCiclo()>0 && d.getIdCiclo()<=Integer.MAX_VALUE,"IdCiclo inválido");
        var c=rCiclo.findById(d.getIdCiclo()).orElseThrow(() -> noEncontrado("Ciclo"));
        var e=id==null?new DetalleDiarioCiclo():rDiario.findById(id).orElseThrow(() -> noEncontrado("Diario"));
        if(id!=null) referencia(e.getCiclo().getIdCiclo(),d.getIdCiclo());
        noFutura(d.getFecha(),"Fecha");energia(d.getNivelEnergia(),5,"NivelEnergia");
        exigir(d.getFaseRegistrada()==null || Set.of("Menstrual","Folicular","Ovulatoria","Lútea").contains(d.getFaseRegistrada()),"Fase no válida");
        texto(d.getObservaciones(),255,"Observaciones",false);
        usuarios.bloquear(c.getUsuario().getIdUsuario()).orElseThrow(() -> noEncontrado("Usuario"));
        conflicto(rDiario.buscarPorUsuarioYFecha(c.getUsuario().getIdUsuario().longValue(),d.getFecha()).stream().anyMatch(v -> !Objects.equals(v.getIdDetalleDiarioCiclo(),id)),"Ya existe un registro diario de esta usuaria para la fecha");
        e.setCiclo(c);e.setFecha(d.getFecha());e.setFaseRegistrada(d.getFaseRegistrada());e.setNivelEnergia(d.getNivelEnergia());e.setObservaciones(d.getObservaciones());
        return dto(rDiario.saveAndFlush(e));
    }
    @Override
    public RutinasDTO guardarRutina(Integer id, RutinasDTO d) {
        exigir(d!=null,"Los datos son obligatorios");identificador(id,d.getIdRutina());
        var u=usuario(d.getIdUsuario());var e=id==null?new Rutinas():rRutina.findById(id).orElseThrow(() -> noEncontrado("Rutina"));
        if(id!=null) referencia(e.getUsuario().getIdUsuario(),d.getIdUsuario());
        texto(d.getNombre(),100,"Nombre",true);texto(d.getObjetivo(),100,"Objetivo",false);texto(d.getNivel(),50,"Nivel",false);
        e.setUsuario(u);e.setNombre(d.getNombre().trim());e.setObjetivo(d.getObjetivo());e.setNivel(d.getNivel());
        if(id==null)e.setFechaCreacion(LocalDateTime.now());
        e.setEstado(d.getEstado()==null?true:d.getEstado());
        return dto(rRutina.saveAndFlush(e));
    }
    @Override
    public RutinaEjerciciosDTO guardarAsignacion(Integer id, RutinaEjerciciosDTO d) {
        exigir(d!=null,"Los datos son obligatorios");identificador(id,d.idRutinaEjercicio());
        positivo(d.idRutina(),"IdRutina");positivo(d.idEjercicio(),"IdEjercicio");
        var r=rRutina.findById(d.idRutina()).orElseThrow(() -> noEncontrado("Rutina"));
        var x=ejercicios.findById(d.idEjercicio()).orElseThrow(() -> noEncontrado("Ejercicio"));
        var e=id==null?new RutinaEjercicios():rAsignacion.findById(id).orElseThrow(() -> noEncontrado("Asignación"));
        if(id!=null){referencia(e.getRutina().getIdRutina(),d.idRutina());referencia(e.getEjercicio().getIdEjercicio(),d.idEjercicio());}
        if(d.series()!=null)positivo(d.series(),"Series");if(d.repeticiones()!=null)positivo(d.repeticiones(),"Repeticiones");
        exigir(d.descansoSeg()==null || d.descansoSeg()>=0,"DescansoSeg no puede ser negativo");
        conflicto(rAsignacion.findByRutina_IdRutinaOrderByIdRutinaEjercicioAsc(d.idRutina()).stream().anyMatch(v -> v.getEjercicio().getIdEjercicio().equals(d.idEjercicio()) && !Objects.equals(v.getIdRutinaEjercicio(),id)),"El ejercicio ya pertenece a esta rutina");
        e.setRutina(r);e.setEjercicio(x);e.setSeries(d.series());e.setRepeticiones(d.repeticiones());e.setDescansoSeg(d.descansoSeg());
        return dto(rAsignacion.saveAndFlush(e));
    }
    @Override
    public SesionesEntrenamientoDTO guardarSesion(Integer id, SesionesEntrenamientoDTO d) {
        exigir(d!=null,"Los datos son obligatorios");identificador(id,d.getIdSesion());positivo(d.getIdRutina(),"IdRutina");
        var r=rRutina.findById(d.getIdRutina()).orElseThrow(() -> noEncontrado("Rutina"));
        if(d.getIdUsuario()!=null)referencia(r.getUsuario().getIdUsuario(),d.getIdUsuario());
        if(id==null){
            exigir(d.getEstado()==null || "EN_CURSO".equals(d.getEstado()),"Una sesión nueva debe iniciarse EN_CURSO");
            conflicto(Boolean.FALSE.equals(r.getEstado()),"La rutina no está activa");
            usuarios.bloquear(r.getUsuario().getIdUsuario()).orElseThrow(() -> noEncontrado("Usuario"));
            conflicto(rSesion.existsByUsuario_IdUsuarioAndRutina_IdRutinaAndEstado(r.getUsuario().getIdUsuario(),r.getIdRutina(),"EN_CURSO"),"Ya existe una sesión en curso para esta rutina");
            var e=rSesion.saveAndFlush(new SesionesEntrenamiento(r,r.getUsuario(),LocalDateTime.now(),null,null,null,"EN_CURSO"));
            for(var a:rAsignacion.findByRutina_IdRutinaOrderByIdRutinaEjercicioAsc(r.getIdRutina()))
                if(!rDetalle.existsBySesion_IdSesionAndEjercicio_IdEjercicio(e.getIdSesion(),a.getEjercicio().getIdEjercicio()))rDetalle.save(new DetalleSesion(e,a.getEjercicio(),null));
            rDetalle.flush();return dto(e);
        }
        var e=rSesion.findById(id).orElseThrow(() -> noEncontrado("Sesión"));
        referencia(e.getRutina().getIdRutina(),d.getIdRutina());referencia(e.getUsuario().getIdUsuario(),r.getUsuario().getIdUsuario());enCurso(e);
        exigir("FINALIZADA".equals(d.getEstado()),"La actualización disponible es finalizar una sesión EN_CURSO");
        positivo(d.getDuracionMin(),"DuracionMin");energia(d.getNivelEnergia(),5,"NivelEnergia");energia(d.getEsfuerzoPercibido(),10,"EsfuerzoPercibido");
        e.setDuracionMin(d.getDuracionMin());e.setNivelEnergia(d.getNivelEnergia());e.setEsfuerzoPercibido(d.getEsfuerzoPercibido());e.setEstado("FINALIZADA");
        return dto(rSesion.saveAndFlush(e));
    }
    @Override
    public DetalleSesionDTO guardarDetalle(Integer id, DetalleSesionDTO d) {
        exigir(d!=null,"Los datos son obligatorios");identificador(id,d.idDetalle());positivo(d.idSesion(),"IdSesion");positivo(d.idEjercicio(),"IdEjercicio");
        var s=rSesion.findById(d.idSesion()).orElseThrow(() -> noEncontrado("Sesión"));enCurso(s);
        var x=ejercicios.findById(d.idEjercicio()).orElseThrow(() -> noEncontrado("Ejercicio"));
        var e=id==null?new DetalleSesion():rDetalle.findById(id).orElseThrow(() -> noEncontrado("Detalle"));
        if(id!=null){referencia(e.getSesion().getIdSesion(),d.idSesion());referencia(e.getEjercicio().getIdEjercicio(),d.idEjercicio());}
        else conflicto(rDetalle.existsBySesion_IdSesionAndEjercicio_IdEjercicio(d.idSesion(),d.idEjercicio()),"El ejercicio ya pertenece a esta sesión");
        texto(d.observacion(),255,"Observacion",false);e.setSesion(s);e.setEjercicio(x);e.setObservacion(d.observacion());
        return dto(rDetalle.saveAndFlush(e));
    }
    @Override
    public DetalleSerieDTO guardarSerie(Integer id, DetalleSerieDTO d) {
        exigir(d!=null,"Los datos son obligatorios");identificador(id,d.idSerie());positivo(d.idDetalle(),"IdDetalle");
        var detalle=rDetalle.bloquear(d.idDetalle()).orElseThrow(() -> noEncontrado("Detalle"));enCurso(detalle.getSesion());
        positivo(d.numeroSerie(),"NumeroSerie");positivo(d.repeticiones(),"Repeticiones");decimal(d.pesoKg(),"PesoKg",true,true);
        var e=id==null?new DetalleSerie():rSerie.findById(id).orElseThrow(() -> noEncontrado("Serie"));
        if(id!=null){referencia(e.getDetalle().getIdDetalle(),d.idDetalle());referencia(e.getNumeroSerie(),d.numeroSerie());}
        else conflicto(rSerie.existsByDetalle_IdDetalleAndNumeroSerie(d.idDetalle(),d.numeroSerie()),"Número de serie ya registrado");
        e.setDetalle(detalle);e.setNumeroSerie(d.numeroSerie());e.setRepeticiones(d.repeticiones());e.setPesoKg(d.pesoKg());
        return dto(rSerie.saveAndFlush(e));
    }
    @Override
    public ProgresoDTO guardarProgreso(Integer id, ProgresoDTO d) {
        exigir(d!=null,"Los datos son obligatorios");identificador(id,d.idProgreso());var u=usuario(d.idUsuario());
        var e=id==null?new Progreso():rProgreso.findById(id).orElseThrow(() -> noEncontrado("Progreso"));
        if(id!=null)referencia(e.getUsuario().getIdUsuario(),d.idUsuario());
        decimal(d.pesoKg(),"PesoKg",true,false);decimal(d.medidaOpcional(),"MedidaOpcional",false,false);texto(d.notaPersonal(),255,"NotaPersonal",false);
        var fecha=d.fecha()==null?(id==null?LocalDate.now():e.getFecha()):d.fecha();noFutura(fecha,"Fecha");
        e.setUsuario(u);e.setFecha(fecha);e.setPesoKg(d.pesoKg());e.setMedidaOpcional(d.medidaOpcional());e.setNotaPersonal(d.notaPersonal());
        return dto(rProgreso.saveAndFlush(e));
    }
    @Override
    public PerfilEntrenamientoDTO guardarPerfil(Integer id, PerfilEntrenamientoDTO d) {
        exigir(d!=null,"Los datos son obligatorios");identificador(id,d.idPerfil());var u=usuario(d.idUsuario());
        var e=id==null?new PerfilEntrenamiento():rPerfil.findById(id).orElseThrow(() -> noEncontrado("Perfil"));
        if(id!=null)referencia(e.getUsuario().getIdUsuario(),d.idUsuario());
        usuarios.bloquear(d.idUsuario()).orElseThrow(() -> noEncontrado("Usuario"));
        conflicto(rPerfil.findByUsuario_IdUsuario(d.idUsuario()).filter(v -> !Objects.equals(v.getIdPerfil(),id)).isPresent(),"La usuaria ya tiene un perfil");
        texto(d.nivelEntrenamiento(),50,"NivelEntrenamiento",true);texto(d.objetivoPrincipal(),100,"ObjetivoPrincipal",true);
        exigir(d.diasDisponibles()!=null && d.diasDisponibles()>=1 && d.diasDisponibles()<=7,"DiasDisponibles debe estar entre 1 y 7");positivo(d.tiempoDisponible(),"TiempoDisponible");noFutura(d.fechaNacimiento(),"FechaNacimiento");
        e.setUsuario(u);e.setNivelEntrenamiento(d.nivelEntrenamiento());e.setObjetivoPrincipal(d.objetivoPrincipal());e.setDiasDisponibles(d.diasDisponibles());e.setTiempoDisponible(d.tiempoDisponible());e.setFechaNacimiento(d.fechaNacimiento());
        return dto(rPerfil.saveAndFlush(e));
    }
    @Override
    public RecomendacionesIADTO guardarRecomendacion(Integer id, RecomendacionesIADTO d) {
        exigir(d!=null,"Los datos son obligatorios");identificador(id,d.idRecomendacion());var u=usuario(d.idUsuario());
        var e=id==null?new RecomendacionesIA():rRecomendacion.findById(id).orElseThrow(() -> noEncontrado("Recomendación"));
        if(id!=null){referencia(e.getUsuario().getIdUsuario(),d.idUsuario());referencia(e.getRutina()==null?null:e.getRutina().getIdRutina(),d.idRutina());}
        Rutinas r=null;
        if(d.idRutina()!=null){positivo(d.idRutina(),"IdRutina");r=rRutina.findById(d.idRutina()).orElseThrow(() -> noEncontrado("Rutina"));referencia(u.getIdUsuario(),r.getUsuario().getIdUsuario());}
        texto(d.tipo(),50,"Tipo",true);texto(d.contenido(),1000000,"Contenido",true);texto(d.motivo(),1000000,"Motivo",false);
        e.setUsuario(u);e.setRutina(r);if(id==null)e.setFecha(LocalDateTime.now());e.setTipo(d.tipo());e.setContenido(d.contenido());e.setMotivo(d.motivo());e.setAceptada(d.aceptada()==null?false:d.aceptada());
        return dto(rRecomendacion.saveAndFlush(e));
    }
    private CiclosDTO dto(Ciclos e){return new CiclosDTO(e.getIdCiclo(),e.getUsuario().getIdUsuario().longValue(),e.getFechaInicio(),e.getFechaFinEstimada(),e.getFechaReal());}
    private DetalleDiarioCicloDTO dto(DetalleDiarioCiclo e){return new DetalleDiarioCicloDTO(e.getIdDetalleDiarioCiclo(),e.getCiclo().getIdCiclo(),e.getFecha(),e.getFaseRegistrada(),e.getNivelEnergia(),e.getObservaciones());}
    private RutinasDTO dto(Rutinas e){return new RutinasDTO(e.getIdRutina(),e.getUsuario().getIdUsuario(),e.getNombre(),e.getObjetivo(),e.getNivel(),e.getFechaCreacion(),e.getEstado());}
    private RutinaEjerciciosDTO dto(RutinaEjercicios e){return new RutinaEjerciciosDTO(e.getIdRutinaEjercicio(),e.getRutina().getIdRutina(),e.getEjercicio().getIdEjercicio(),e.getSeries(),e.getRepeticiones(),e.getDescansoSeg());}
    private SesionesEntrenamientoDTO dto(SesionesEntrenamiento e){return new SesionesEntrenamientoDTO(e.getIdSesion(),e.getRutina().getIdRutina(),e.getUsuario().getIdUsuario(),e.getFecha(),e.getDuracionMin(),e.getNivelEnergia(),e.getEsfuerzoPercibido(),e.getEstado());}
    private DetalleSesionDTO dto(DetalleSesion e){return new DetalleSesionDTO(e.getIdDetalle(),e.getSesion().getIdSesion(),e.getEjercicio().getIdEjercicio(),e.getObservacion());}
    private DetalleSerieDTO dto(DetalleSerie e){return new DetalleSerieDTO(e.getIdSerie(),e.getDetalle().getIdDetalle(),e.getNumeroSerie(),e.getRepeticiones(),e.getPesoKg());}
    private ProgresoDTO dto(Progreso e){return new ProgresoDTO(e.getIdProgreso(),e.getUsuario().getIdUsuario(),e.getFecha(),e.getPesoKg(),e.getMedidaOpcional(),e.getNotaPersonal());}
    private PerfilEntrenamientoDTO dto(PerfilEntrenamiento e){return new PerfilEntrenamientoDTO(e.getIdPerfil(),e.getUsuario().getIdUsuario(),e.getNivelEntrenamiento(),e.getObjetivoPrincipal(),e.getDiasDisponibles(),e.getTiempoDisponible(),e.getFechaNacimiento());}
    private RecomendacionesIADTO dto(RecomendacionesIA e){return new RecomendacionesIADTO(e.getIdRecomendacion(),e.getUsuario().getIdUsuario(),e.getRutina()==null?null:e.getRutina().getIdRutina(),e.getFecha(),e.getTipo(),e.getContenido(),e.getMotivo(),e.getAceptada());}
}
