package pe.edu.upc.femfitai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upc.femfitai.entities.*;
import pe.edu.upc.femfitai.repositories.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import javax.sql.DataSource;
import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties="spring.config.location=classpath:/application-test.properties")
class AdministracionIntegrationTest {
    @LocalServerPort int port;
    @Autowired UsuariosRepository usuarios;
    @Autowired RutinasRepository rutinas;
    @Autowired EjerciciosRepository ejercicios;
    @Autowired ProgresoRepository progresos;
    @Autowired DetalleSesionRepository detalles;
    @Autowired PasswordEncoder encoder;
    @Autowired DataSource dataSource;
    final HttpClient client=HttpClient.newHttpClient();
    final JsonMapper json=JsonMapper.builder().build();
    final String password="SyntheticTestOnly123!";
    Usuarios owner,other;
    String adminToken,userToken;
    final List<String> modulos=List.of("ciclos","detalle-diario","rutinas","rutina-ejercicios","sesiones","detalle-sesion","detalle-serie","progreso","perfiles","recomendaciones");

    @BeforeEach void preparar() throws Exception {
        owner=usuario("USUARIA");other=usuario("USUARIA");
        adminToken=login(usuario("ADMIN"));userToken=login(owner);
    }
    Usuarios usuario(String rol){return usuarios.saveAndFlush(new Usuarios("Test","Admin",UUID.randomUUID()+"@example.test",encoder.encode(password),rol,true,LocalDateTime.now()));}
    String login(Usuarios u) throws Exception {
        var r=request("POST","/login",Map.of("correo",u.getCorreo(),"password",password),null);
        assertEquals(200,r.statusCode(),r.body());return json.readTree(r.body()).get("accessToken").asText();
    }
    HttpResponse<String> request(String method,String path,Object body,String token) throws Exception {
        var b=HttpRequest.newBuilder(URI.create("http://localhost:"+port+path)).timeout(Duration.ofSeconds(15)).header("Content-Type","application/json");
        if(token!=null)b.header("Authorization","Bearer "+token);
        return client.send(b.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build(),HttpResponse.BodyHandlers.ofString());
    }
    JsonNode call(int status,String method,String path,Object body) throws Exception {
        var r=request(method,path,body,adminToken);assertEquals(status,r.statusCode(),r.body());
        return r.body().isBlank()?null:json.readTree(r.body());
    }
    String path(String modulo){return "/administracion/"+modulo;}
    int rutina(Usuarios u) throws Exception {return call(201,"POST",path("rutinas"),Map.of("idUsuario",u.getIdUsuario(),"nombre","Plan real de prueba","estado",true)).get("idRutina").asInt();}
    int ejercicio(){return ejercicios.saveAndFlush(new Ejercicios("Ejercicio de prueba","Piernas","Fuerza","Prueba aislada H2")).getIdEjercicio();}
    Map<String,Object> progreso(Usuarios u){return Map.of("idUsuario",u.getIdUsuario(),"fecha",LocalDate.now().toString(),"pesoKg",60.25,"notaPersonal","Registro de prueba H2");}

    @Test void pruebasUsanExclusivamenteH2() throws Exception {
        try(var c=dataSource.getConnection()){assertTrue(c.getMetaData().getURL().startsWith("jdbc:h2:mem:"));}
    }
    @Test void todosLosModulosExigenAdminYToken() throws Exception {
        for(String m:modulos){
            assertEquals(401,request("GET",path(m),null,null).statusCode(),m);
            assertEquals(403,request("GET",path(m),null,userToken).statusCode(),m);
            assertEquals(403,request("POST",path(m),Map.of(),userToken).statusCode(),m);
            assertEquals(403,request("PUT",path(m)+"/1",Map.of(),userToken).statusCode(),m);
            assertEquals(403,request("DELETE",path(m)+"/1",null,userToken).statusCode(),m);
            call(200,"GET",path(m),null);
        }
        assertEquals(403,request("GET","/ciclos",null,adminToken).statusCode());
        call(404,"GET",path("desconocido"),null);
    }
    @Test void listaPaginadaFiltraUsuariaYNoExponeCredenciales() throws Exception {
        call(201,"POST",path("progreso"),progreso(owner));call(201,"POST",path("progreso"),progreso(other));
        var page=call(200,"GET",path("progreso")+"?idUsuario="+owner.getIdUsuario()+"&tamano=1",null);
        assertEquals(1,page.get("content").size());assertEquals(owner.getIdUsuario(),page.get("content").get(0).get("idUsuario").asInt());
        assertFalse(page.toString().contains("password"));assertFalse(page.toString().contains("PasswordHash"));
        call(400,"GET",path("progreso")+"?pagina=-1",null);call(400,"GET",path("progreso")+"?tamano=101",null);call(400,"GET",path("progreso")+"?idUsuario=0",null);
    }
    @Test void ciclosPermitenCrudYRespetanFechasPropietariaYCicloActivo() throws Exception {
        var body=new HashMap<String,Object>();body.put("idUsuario",owner.getIdUsuario());body.put("fechaInicio",LocalDate.now().minusDays(3).toString());
        var c=call(201,"POST",path("ciclos"),body);long id=c.get("idCiclo").asLong();
        assertEquals(LocalDate.now().plusDays(25).toString(),c.get("fechaFinEstimada").asText());
        call(409,"POST",path("ciclos"),body);call(200,"GET",path("ciclos")+"/"+id,null);
        body.put("idUsuario",other.getIdUsuario());call(400,"PUT",path("ciclos")+"/"+id,body);body.put("idUsuario",owner.getIdUsuario());
        body.put("fechaReal",LocalDate.now().toString());call(200,"PUT",path("ciclos")+"/"+id,body);
        call(204,"DELETE",path("ciclos")+"/"+id,null);call(404,"GET",path("ciclos")+"/"+id,null);
        body.put("fechaInicio",LocalDate.now().plusDays(1).toString());call(400,"POST",path("ciclos"),body);
    }
    @Test void diarioValidaFechaEnergiaDuplicadosYProtegeCicloRelacionado() throws Exception {
        long ciclo=call(201,"POST",path("ciclos"),Map.of("idUsuario",owner.getIdUsuario(),"fechaInicio",LocalDate.now().minusDays(2).toString())).get("idCiclo").asLong();
        var body=new HashMap<String,Object>();body.put("idCiclo",ciclo);body.put("fecha",LocalDate.now().toString());body.put("faseRegistrada","Menstrual");body.put("nivelEnergia",3);
        int id=call(201,"POST",path("detalle-diario"),body).get("idDetalleDiarioCiclo").asInt();
        call(409,"POST",path("detalle-diario"),body);call(409,"DELETE",path("ciclos")+"/"+ciclo,null);
        body.put("observaciones","Actualizado");call(200,"PUT",path("detalle-diario")+"/"+id,body);
        body.put("nivelEnergia",6);call(400,"PUT",path("detalle-diario")+"/"+id,body);
        call(204,"DELETE",path("detalle-diario")+"/"+id,null);call(204,"DELETE",path("ciclos")+"/"+ciclo,null);
    }
    @Test void rutinasYAsignacionesTienenCrudSinBorrarReferencias() throws Exception {
        int rutina=rutina(owner),ejercicio=ejercicio();
        var a=new HashMap<String,Object>();a.put("idRutina",rutina);a.put("idEjercicio",ejercicio);a.put("series",3);a.put("repeticiones",12);a.put("descansoSeg",30);
        int id=call(201,"POST",path("rutina-ejercicios"),a).get("idRutinaEjercicio").asInt();
        call(409,"POST",path("rutina-ejercicios"),a);call(409,"DELETE",path("rutinas")+"/"+rutina,null);
        a.put("series",4);assertEquals(4,call(200,"PUT",path("rutina-ejercicios")+"/"+id,a).get("series").asInt());
        call(200,"PUT",path("rutinas")+"/"+rutina,Map.of("idUsuario",owner.getIdUsuario(),"nombre","Rutina actualizada","estado",false));
        call(204,"DELETE",path("rutina-ejercicios")+"/"+id,null);call(204,"DELETE",path("rutinas")+"/"+rutina,null);
    }
    @Test void sesionesSeInicianDesdeRutinaClonanDetallesYConservanHistorial() throws Exception {
        int rutina=rutina(owner),ejercicio=ejercicio();
        call(201,"POST",path("rutina-ejercicios"),Map.of("idRutina",rutina,"idEjercicio",ejercicio,"series",3));
        var datos=new HashMap<String,Object>();datos.put("idRutina",rutina);datos.put("idUsuario",owner.getIdUsuario());
        int sesion=call(201,"POST",path("sesiones"),datos).get("idSesion").asInt();
        assertTrue(detalles.existsBySesion_IdSesionAndEjercicio_IdEjercicio(sesion,ejercicio));
        call(409,"POST",path("sesiones"),datos);call(405,"DELETE",path("sesiones")+"/"+sesion,null);
        datos.put("estado","FINALIZADA");datos.put("duracionMin",30);datos.put("nivelEnergia",4);datos.put("esfuerzoPercibido",6);
        assertEquals("FINALIZADA",call(200,"PUT",path("sesiones")+"/"+sesion,datos).get("estado").asText());
        call(409,"PUT",path("sesiones")+"/"+sesion,datos);
        var detalle=detalles.findAll().stream().filter(v->v.getSesion().getIdSesion().equals(sesion)).findFirst().orElseThrow();
        call(409,"DELETE",path("detalle-sesion")+"/"+detalle.getIdDetalle(),null);
    }
    @Test void detallesYSeriesTienenCrudSoloDuranteSesionEnCurso() throws Exception {
        int rutina=rutina(owner),ejercicio=ejercicio();
        int sesion=call(201,"POST",path("sesiones"),Map.of("idRutina",rutina)).get("idSesion").asInt();
        var d=Map.of("idSesion",sesion,"idEjercicio",ejercicio,"observacion","Primera observación");
        int detalle=call(201,"POST",path("detalle-sesion"),d).get("idDetalle").asInt();
        call(200,"PUT",path("detalle-sesion")+"/"+detalle,d);call(409,"POST",path("detalle-sesion"),d);
        var serie=new HashMap<String,Object>();serie.put("idDetalle",detalle);serie.put("numeroSerie",1);serie.put("repeticiones",10);serie.put("pesoKg",0);
        int id=call(201,"POST",path("detalle-serie"),serie).get("idSerie").asInt();
        call(409,"POST",path("detalle-serie"),serie);call(409,"DELETE",path("detalle-sesion")+"/"+detalle,null);
        serie.put("pesoKg",12.25);call(200,"PUT",path("detalle-serie")+"/"+id,serie);
        serie.put("numeroSerie",2);call(400,"PUT",path("detalle-serie")+"/"+id,serie);
        call(204,"DELETE",path("detalle-serie")+"/"+id,null);call(204,"DELETE",path("detalle-sesion")+"/"+detalle,null);
    }
    @Test void progresoValidaCamposYNoPermiteTransferirNiRedirigirRegistro() throws Exception {
        var body=new HashMap<String,Object>(progreso(owner));int id=call(201,"POST",path("progreso"),body).get("idProgreso").asInt();
        body.put("pesoKg",62.75);call(200,"PUT",path("progreso")+"/"+id,body);
        body.put("idUsuario",other.getIdUsuario());call(400,"PUT",path("progreso")+"/"+id,body);body.put("idUsuario",owner.getIdUsuario());
        body.put("idProgreso",id+10000);call(400,"PUT",path("progreso")+"/"+id,body);body.remove("idProgreso");
        body.put("pesoKg",-1);call(400,"PUT",path("progreso")+"/"+id,body);assertEquals(0,progresos.findById(id).orElseThrow().getPesoKg().compareTo(new BigDecimal("62.75")));
        assertEquals(403,request("PUT",path("progreso")+"/"+id,progreso(owner),userToken).statusCode());
        call(204,"DELETE",path("progreso")+"/"+id,null);
    }
    @Test void perfilTieneCrudUnicoPorUsuariaYValidaDisponibilidad() throws Exception {
        var body=new HashMap<String,Object>();body.put("idUsuario",owner.getIdUsuario());body.put("nivelEntrenamiento","Inicial");body.put("objetivoPrincipal","Fuerza");body.put("diasDisponibles",3);body.put("tiempoDisponible",45);body.put("fechaNacimiento","2000-01-01");
        int id=call(201,"POST",path("perfiles"),body).get("idPerfil").asInt();call(409,"POST",path("perfiles"),body);
        body.put("diasDisponibles",7);call(200,"PUT",path("perfiles")+"/"+id,body);body.put("diasDisponibles",8);call(400,"PUT",path("perfiles")+"/"+id,body);
        call(204,"DELETE",path("perfiles")+"/"+id,null);
    }
    @Test void recomendacionesTienenCrudYNoCruzanRutinasDeOtrasUsuarias() throws Exception {
        int rutina=rutina(owner),ajena=rutina(other);
        var body=new HashMap<String,Object>();body.put("idUsuario",owner.getIdUsuario());body.put("idRutina",ajena);body.put("tipo","Administrativa");body.put("contenido","Nota manual de prueba");
        call(400,"POST",path("recomendaciones"),body);body.put("idRutina",rutina);
        int id=call(201,"POST",path("recomendaciones"),body).get("idRecomendacion").asInt();
        body.put("contenido","Contenido corregido");call(200,"PUT",path("recomendaciones")+"/"+id,body);
        call(409,"DELETE",path("rutinas")+"/"+rutina,null);call(204,"DELETE",path("recomendaciones")+"/"+id,null);
    }
    @Test void usuariasConservanAislamientoDeRegistrosPersonales() throws Exception {
        int ajeno=call(201,"POST",path("progreso"),progreso(other)).get("idProgreso").asInt();
        for(String method:List.of("GET","PUT","DELETE"))assertEquals(403,request(method,"/progreso/"+ajeno,method.equals("PUT")?progreso(owner):null,userToken).statusCode());
        call(404,"GET",path("progreso")+"/2147483647",null);call(400,"GET",path("progreso")+"/0",null);
    }

    @Test void diarioGeneralSoloDevuelveRegistrosDeLaUsuariaAutenticada() throws Exception {
        long propio=call(201,"POST",path("ciclos"),Map.of("idUsuario",owner.getIdUsuario(),"fechaInicio",LocalDate.now().toString())).get("idCiclo").asLong();
        long ajeno=call(201,"POST",path("ciclos"),Map.of("idUsuario",other.getIdUsuario(),"fechaInicio",LocalDate.now().toString())).get("idCiclo").asLong();
        for(long ciclo:List.of(propio,ajeno))call(201,"POST",path("detalle-diario"),Map.of("idCiclo",ciclo,"fecha",LocalDate.now().toString(),"nivelEnergia",3));
        var respuesta=request("GET","/detalle-diario",null,userToken);assertEquals(200,respuesta.statusCode());
        var filas=json.readTree(respuesta.body());assertEquals(1,filas.size());assertEquals(propio,filas.get(0).get("idCiclo").asLong());
        var otra=request("GET","/detalle-diario",null,login(other));assertEquals(200,otra.statusCode());
        assertEquals(ajeno,json.readTree(otra.body()).get(0).get("idCiclo").asLong());
    }
    @Test void diarioPorCicloVerificaPropiedadInclusoSiNoTieneEntradas() throws Exception {
        long propio=call(201,"POST",path("ciclos"),Map.of("idUsuario",owner.getIdUsuario(),"fechaInicio",LocalDate.now().toString())).get("idCiclo").asLong();
        long ajeno=call(201,"POST",path("ciclos"),Map.of("idUsuario",other.getIdUsuario(),"fechaInicio",LocalDate.now().toString())).get("idCiclo").asLong();
        assertEquals(200,request("GET","/detalle-diario/ciclo/"+propio,null,userToken).statusCode());
        assertEquals(403,request("GET","/detalle-diario/ciclo/"+ajeno,null,userToken).statusCode());
        assertEquals(404,request("GET","/detalle-diario/ciclo/2147483647",null,userToken).statusCode());
        assertEquals(400,request("GET","/detalle-diario/ciclo/2147483648",null,userToken).statusCode());
        assertEquals(401,request("GET","/detalle-diario/ciclo/"+propio,null,null).statusCode());
        assertEquals(403,request("GET","/detalle-diario/ciclo/"+propio,null,adminToken).statusCode());
    }
    @Test void detalleDeSesionNoExponeElJoinDeOtraUsuaria() throws Exception {
        int propia=rutina(owner),ajena=rutina(other),ejercicio=ejercicio();
        for(int r:List.of(propia,ajena))call(201,"POST",path("rutina-ejercicios"),Map.of("idRutina",r,"idEjercicio",ejercicio));
        int sesionPropia=call(201,"POST",path("sesiones"),Map.of("idRutina",propia)).get("idSesion").asInt();
        int sesionAjena=call(201,"POST",path("sesiones"),Map.of("idRutina",ajena)).get("idSesion").asInt();
        assertEquals(200,request("GET","/sesiones/detalle/"+sesionPropia,null,userToken).statusCode());
        assertEquals(403,request("GET","/sesiones/detalle/"+sesionAjena,null,userToken).statusCode());
        assertEquals(403,request("GET","/sesiones/detalle/"+sesionPropia,null,login(other)).statusCode());
        assertEquals(404,request("GET","/sesiones/detalle/2147483647",null,userToken).statusCode());
        assertEquals(401,request("GET","/sesiones/detalle/"+sesionPropia,null,null).statusCode());
    }
    @Test void ejerciciosPorUsuariaProtegeElJoinYConservaElCatalogoCompartido() throws Exception {
        int propia=rutina(owner),ajena=rutina(other),ejercicio=ejercicio();
        for(int r:List.of(propia,ajena))call(201,"POST",path("rutina-ejercicios"),Map.of("idRutina",r,"idEjercicio",ejercicio));
        var respuesta=request("GET","/ejercicios/usuario/"+owner.getIdUsuario(),null,userToken);assertEquals(200,respuesta.statusCode());
        var filas=json.readTree(respuesta.body());assertEquals(1,filas.size());assertEquals(owner.getIdUsuario(),filas.get(0).get("idUsuario").asInt());
        assertEquals(403,request("GET","/ejercicios/usuario/"+other.getIdUsuario(),null,userToken).statusCode());
        assertEquals(403,request("GET","/ejercicios/usuario/"+owner.getIdUsuario(),null,login(other)).statusCode());
        assertEquals(401,request("GET","/ejercicios/usuario/"+owner.getIdUsuario(),null,null).statusCode());
        assertEquals(200,request("GET","/ejercicios",null,userToken).statusCode());
        call(200,"GET","/ejercicios",null);
    }
}
