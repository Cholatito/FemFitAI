# Revisión del backend — 25 de septiembre de 2026

Fuente funcional: tablero FemFitIA, https://trello.com/b/Jxj8dmaI/femfitia.
Se leyeron las 60 tarjetas agrupadas en los tres sprints. US42 y US43
no tienen checklists que concreten sus definiciones pendientes.
No se modificó Trello. Este informe distingue backend verificado de cierre
integral de una historia con criterios de interfaz o infraestructura.

## Resultado por sprint

| SPRINT | US | ESTADO FINAL | QUÉ SE HIZO | QUÉ FALTA |
| --- | --- | --- | --- | --- |
| 1 | US11 | Backend funcional verificado | Se mantuvo el guardado; HTTP y persistencia de fase/energía comprobados; diagnóstico H2 | Medición real PostgreSQL/red del criterio menor a 1 segundo; indicadores e interacción frontend |
| 1 | US42 | Implementación conservada; definición pendiente | Sin cambios en sesiones; POST/PUT, propiedad, fecha, persistencia y límites actuales 1–10 probados | Trello dice «fuera de escala», sin extremos; confirmar RPE |
| 1 | US43 | Registro/login conservados; definición pendiente | BCrypt, rol USUARIA, duplicidad, login y rechazo de contraseña vacía comprobados | «Complejidad mínima» no fija longitud ni clases de caracteres |
| 1 | US06 | Regla funcional pendiente | Revisado modelo actual y criterios; se conserva FechaFinEstimada recibida | Fórmula aprobada de cálculo a partir de FechaInicio; no se inventó duración de ciclo |
| 1 | US08 | Escala funcional pendiente | Rango actual 1–5 conservado y probado | Confirmación funcional de extremos; validación visual frontend |
| 1 | US01, US02, US04 | Backend existente; sin reescritura | Revisión de control de propiedad y prueba de rechazo de PUT/DELETE ajenos | No se certificó de nuevo cada criterio de creación/cierre/eliminación; integración frontend |
| 1 | US07, US09, US10 | Backend existente conservado | Se mantienen pruebas de fases, energía y actualización concurrente por fecha; se comprobó persistencia de fases | Selectores, avisos y edición visual frontend; revisión integral de UI |
| 1 | US12, US14, US22, US27, US37, US39, US40 | Backend existente conservado | Se mantienen servicios; pruebas de persistencia/consulta y privacidad de progreso, perfil y rutina | Integración frontend; no se declara cierre integral de cada criterio a partir de pruebas compartidas |
| 1 | US32, US33 | Backend verificado | Se mantienen pruebas de permisos ADMIN/PROGRAMADOR y eliminación con referencias | Integración frontend |
| 1 | US44, US45, US46, US49, US50 | Backend verificado en flujos probados | Alta de detalle/serie, PUT sin cambiar referencias, privacidad, eliminación y persistencia | Formularios, cancelación, confirmaciones y actualización visual |
| 2 | US15, US25, US41 | Funcionalidad backend verificada; rendimiento pendiente | Módulos intactos; pruebas existentes y mediciones HTTP H2 adicionales; perfil ausente devuelve 404 | Medición definitiva en PostgreSQL; comportamiento de carga/error frontend |
| 2 | US34 | Backend terminado y revalidado | Sin reescritura; búsqueda, filtros, caracteres literales, paginación y vacíos pasan | Integración frontend si aún no está realizada |
| 2 | US03, US13, US23, US24, US28, US29, US35, US38 | Backend existente conservado | Revisión de rutas y servicios; consultas de perfil, historial y ejercicios de rutina probadas | No se certificó cada criterio individual; integración frontend |
| 2 | US30 | Consulta existente; rendimiento no certificado | Catálogo conservado sin optimizaciones especulativas | Diagnóstico específico y medición PostgreSQL; carga y errores frontend |
| 2 | US31 | Presentación pendiente de frontend | DTO de ejercicios conservado | Legibilidad, descripciones extensas y presentación visual |
| 2 | US51 | Backend verificado | DELETE de detalle: 403 ajeno, 409 con series, 204 tras retirar series; persistencia comprobada | Confirmación/cancelación y actualización de la vista |
| 3 | US05, US16 | Privacidad backend verificada | Consultas ajenas bloqueadas, listados filtrados y PUT sin transferencia | Integración frontend; no se auditó una aplicación frontend en este repositorio |
| 3 | US17, US20 | Generación interna basada en perfil implementada | Usa nivel, objetivo, días y tiempo del perfil; valida datos mínimos y persiste una recomendación para la cuenta autenticada | Confirmar que el formato basado en reglas satisface el producto; Trello no exige proveedor IA; H2 no certifica rendimiento; UI no bloqueante |
| 3 | US18, US19 | Backend terminado y revalidado | Implementación intacta; consulta privada, vacío y aceptación idempotente; interoperabilidad con el generador probada | Interfaz de consulta y aceptación |
| 3 | US21 | Contenido basado en perfil; revisión visual pendiente | La salida incluye sugerencia y motivo derivados de datos del perfil | Aceptación del estilo de redacción y presentación frontend |
| 3 | US26, US36 | Backend existente conservado | Integridad de relaciones y valores revisada; JOIN y rechazo de eliminación referenciada probados | No se certificó nuevamente cada criterio individual en PostgreSQL |
| 3 | US54, US59 | Autenticación backend verificada | Login, hashing y JWT válidos, vencidos, alterados y ausentes probados; se cerró acceso ajeno GET /usuarios/{id} conservando administración | Configuración segura del entorno desplegado e integración cliente |
| 3 | US60 | Manejador existente revalidado | Sin reescritura; errores controlados y fallo inesperado usan JSON centralizado; detalle interno no se expone | Validación operativa de logs en despliegue |
| 3 | US47, US48, US52, US53 | Frontend pendiente | Sin cambios artificiales de backend para criterios de formulario/interacción | Conservación de entradas, bloqueo de caracteres/doble envío y diálogos |
| 3 | US55, US56, US57, US58 | Infraestructura pendiente | Revisados criterios y Dockerfile existente; sin despliegue ni cambios remotos | Render, secretos reales, HTTPS/certificados, CI/CD, respaldos y restauración; corregir referencias C#/.NET/SQL Server frente al proyecto Java/PostgreSQL |

## Definiciones que impiden cerrar historias

- [US42](https://trello.com/c/WuWZDvsg): CA05 no confirma 1–10. Se mantiene la validación y se prueban los extremos actuales sin declararlos definitivos.
- [US43](https://trello.com/c/SzexNvYi): CA06 solo menciona complejidad mínima. Falta longitud mínima/máxima y clases de caracteres si son requeridas. No se añade política nueva.
- US06: falta la fórmula de FechaFinEstimada. US08: falta el rango oficial de energía. Son hallazgos adicionales; se conserva la implementación.
- [US17](https://trello.com/c/Q363IYFx): no define proveedor IA ni precisa los datos mínimos. Se implementó una regla interna usando los campos existentes del perfil; confirmar si el producto acepta ese formato basado en reglas.
- [US20](https://trello.com/c/nlUkE51C): «tiempo breve» no fija umbral ni carga. Hay un generador interno; H2 solo aporta muestras diagnósticas. La disponibilidad de la interfaz corresponde al frontend.

### US06 — requiere definición funcional

Estado: **pendiente de definición funcional; cálculo automático no implementado**.
La [tarjeta US06](https://trello.com/c/LRLHDrAK) exige en CA02 calcular
FechaFinEstimada a partir de FechaInicio «según la regla de negocio definida»,
pero no especifica esa regla. La revisión del código y la documentación actuales
tampoco encontró una fórmula explícita; la tarjeta no aporta comentarios ni
checklists que la completen.

Se necesita definir la fórmula y sus datos de entrada, cómo se cuentan los días
y qué debe ocurrir cuando cambie FechaInicio o falten datos para calcular.
No se asume una duración fija ni un promedio del historial.

El backend actual conserva FechaFinEstimada como campo opcional recibido en
POST/PUT, valida que no sea anterior a FechaInicio, lo persiste y lo incluye
en los DTO de respuesta. Esto no equivale a cumplir el cálculo automático.
Se mantienen las validaciones de fecha y propiedad existentes. Una vez aprobada
la regla, corresponderá implementar el cálculo y sus pruebas de validación y
persistencia en el entorno aislado H2, conservando las pruebas actuales.

Esta aclaración solo modifica documentación; no cambia código, pruebas ni
application.properties y no certifica una nueva ejecución de la suite.

### US08 — pendiente funcional: confirmar escala de energía

La [tarjeta US08](https://trello.com/c/4ppWI4Lo) exige un valor obligatorio
dentro de la escala permitida, pero no define sus extremos. Los criterios
disponibles no confirman explícitamente el rango 1–5.

Se conserva la escala actual **1–5**, sus validaciones y un comentario en código
que deja constancia de que faltan los extremos funcionales. No se inventa otra
escala ni se declara la validación definitiva. Falta la confirmación de los valores
mínimo y máximo permitidos para cerrar este requisito.

Se mantienen las pruebas existentes de persistencia del valor 3 y rechazo
de 0, 6 y null. Los criterios de validación visual corresponden al frontend.

## Contrato preparado para generación

`POST /recomendaciones/generar`, autenticado, recibe `{}` o `{"idRutina":123}`.
El usuario se obtiene del token. La referencia a rutina, si está presente,
debe existir y pertenecer a esa cuenta. Los IDs nuevos usan Integer como
RecomendacionesIA, Usuarios y Rutinas. No se migró Ciclos, que mantiene Long
con controles de rango antes de las conversiones existentes.

`PerfilEntrenamientoGeneradorRecomendaciones` implementa una regla interna
determinista: usa nivel, objetivo, días por semana y minutos disponibles del
perfil de la usuaria. Si falta el perfil o algún valor necesario, responde 400
con error descriptivo y no persiste una recomendación incompleta. Con datos
válidos, genera texto basado en esos campos; el servicio lo persiste como
recomendación propia, con fecha actual y estado no aceptado. US18/US19 siguen
funcionando con los registros generados.

La regla actual no invoca un modelo ni proveedor externo y no se presenta como
generación de IA. `GeneradorRecomendaciones` recibe el ID de la cuenta
autenticada y una rutina opcional verificada. Un futuro adaptador podrá sustituir
la regla si se define otro requisito. El servicio comprueba contenido/motivo
no vacíos y tamaño del tipo, persiste propietario/fecha y aceptada=false, y
devuelve 201 con DTO y Location.

La llamada al generador no mantiene abierta una transacción de base de datos.
Los errores controlados y no controlados pasan por GlobalExceptionHandler.
Salida incompleta: 502, sin insertar. No se han definido reintentos, timeouts,
claves, proveedor, colas ni ejecución asíncrona. US20 no fija un umbral temporal;
las mediciones H2 son diagnósticas y la disponibilidad de la interfaz sigue
siendo un criterio frontend.

## Verificación y límites

Ejecutado desde la raíz real `FemFitAI/FemFitAI`:

```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd test
```

- Compilación: BUILD SUCCESS.
- Tests: **37**, failures **0**, errors **0**, skipped **0**.
- BackendIntegrationTest: 24; FemFitAiApplicationTests: 1; GeneracionRecomendacionesIntegrationTest: 6; GeneracionRecomendacionesRealIntegrationTest: 1; PerformanceIntegrationTest: 5.
- Las 13 pruebas originales permanecen; las demás amplían seguridad, persistencia, generación y diagnósticos H2.
- Todos los contextos de prueba usan exclusivamente `spring.config.location=classpath:/application-test.properties` y H2 en memoria.
- No se inició la aplicación con la configuración PostgreSQL personal.
- El fallo interno y el conflicto de unicidad forzados por tests producen logs esperados; no son failures/errors de la suite.
- `git diff --check` sin errores de espacios. Git advierte conversión normal LF/CRLF.

Mediciones diagnósticas de esta ejecución (muestras pequeñas, sin umbrales ni carga real):

| Operación | Tiempo H2 |
| --- | --- |
| POST diario, cuatro muestras HTTP | 20.557, 16.130, 8.795, 9.232 ms |
| GET /progreso HTTP | 6.954 ms |
| GET ejercicios de rutina HTTP | 6.453 ms |
| GET /perfiles HTTP | 4.147 ms |
| Servicio historial con 25 registros | 10.308 ms |
| Servicio JOIN ejercicios de rutina | 11.628 ms |
| Servicio consulta perfil | 2.022 ms |
| Generación basada en perfil, HTTP | 8 ms |
| Catálogo, 100 ejercicios, HTTP | 11 ms |
| Historial, 100 registros, HTTP | 14 ms |

Estas mediciones y las pruebas nuevas de US11/15/25/30/41 no certifican
PostgreSQL, concurrencia real, red ni cumplimiento del límite de US11. En backend,
US11/US15/US25/US41 conservan funcionalidad probada y quedan pendientes de rendimiento PostgreSQL; las historias completas
también contienen criterios frontend. US30 no recibió un diagnóstico específico.

## Archivos

Modificados:

- `Dockerfile`
- `README.md`
- `src/main/java/pe/edu/upc/femfitai/securities/OpenApiConfig.java`
- `src/main/java/pe/edu/upc/femfitai/services/implementations/CiclosService.java` (se conserva entrada de FechaFinEstimada sin fórmula)
- `src/main/java/pe/edu/upc/femfitai/services/implementations/DetalleDiarioCicloService.java`
- `src/main/java/pe/edu/upc/femfitai/services/implementations/EjerciciosService.java`
- `src/main/java/pe/edu/upc/femfitai/services/implementations/GeneracionRecomendacionesService.java`
- `src/main/java/pe/edu/upc/femfitai/services/implementations/SesionesEntrenamientoService.java`
- `src/test/java/pe/edu/upc/femfitai/BackendIntegrationTest.java`
- `src/test/java/pe/edu/upc/femfitai/GeneracionRecomendacionesIntegrationTest.java`
- `src/test/java/pe/edu/upc/femfitai/GeneracionRecomendacionesRealIntegrationTest.java`
- `src/test/java/pe/edu/upc/femfitai/PerformanceIntegrationTest.java`
- `docs/revision-backend-2026-09-25.md`

Nuevos:

- `src/main/java/pe/edu/upc/femfitai/services/implementations/PerfilEntrenamientoGeneradorRecomendaciones.java`
- `src/test/java/pe/edu/upc/femfitai/GeneracionRecomendacionesRealIntegrationTest.java`
- `src/test/java/pe/edu/upc/femfitai/PerformanceIntegrationTest.java`

No se modificó ni incluyó `src/main/resources/application.properties`.
`target/` y sus logs generados tampoco se incluyen.

No se declara una historia integral terminada cuando falta frontend,
infraestructura o definición funcional. Backend con evidencia de cierre en el
alcance revisado: US34, US18, US19, US05, US16, US54, US59 y US60;
otras funcionalidades existentes y sus límites se detallan en la tabla.
