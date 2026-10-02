# FemFitAI

Backend REST para gestionar usuarios, rutinas, ciclos y sesiones de entrenamiento de FemFitAI. Organiza la información de entrenamiento y su persistencia en PostgreSQL.

## Tecnologías

- Java 25.
- Spring Boot 4.1.1.
- Spring Data JPA.
- PostgreSQL.
- Maven, con Maven Wrapper incluido.

## Requisitos

- JDK 25 instalado y `JAVA_HOME` configurado.
- PostgreSQL disponible y una base de datos local con acceso autorizado.
- Conexión a Internet para descargar Maven y las dependencias en la primera compilación.
- Maven instalado solo si prefieres usar `mvn` en lugar del wrapper incluido.

## Configuración local

Desde la raíz del proyecto, copia la plantilla **solo si todavía no tienes** `application.properties`. Conserva tu archivo local si ya existe.

En PowerShell:

```powershell
if (!(Test-Path 'src/main/resources/application.properties')) {
    Copy-Item 'src/main/resources/application.properties.example' 'src/main/resources/application.properties'
}
```

En Linux o macOS:

```sh
if [ ! -f src/main/resources/application.properties ]; then
    cp src/main/resources/application.properties.example src/main/resources/application.properties
fi
```

Edita `src/main/resources/application.properties` y sustituye `TU_BASE_DE_DATOS`, `TU_USUARIO` y `TU_PASSWORD` por tus valores locales. Ajusta también el host y el puerto de PostgreSQL si corresponde.

La plantilla conserva `spring.jpa.hibernate.ddl-auto=update`: al iniciar, Hibernate puede crear o actualizar tablas. Para trabajar con un esquema existente sin modificarlo automáticamente, configura `validate` en tu archivo local; el esquema debe ser compatible con las entidades.

`application.properties` está excluido de Git. Comparte únicamente `application.properties.example`, sin credenciales reales. Los archivos `.env` también están ignorados; la configuración documentada utiliza `application.properties`.

## Compilación y ejecución

Compilar sin iniciar la aplicación ni ejecutar pruebas:

```powershell
.\mvnw.cmd -DskipTests compile
```

Cuando hayas configurado PostgreSQL y revisado la opción de esquema, puedes iniciar el backend con:

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux o macOS, usa `sh ./mvnw` en lugar de `.\mvnw.cmd`. Con Maven instalado, puedes usar `mvn`. Por defecto, la API estará disponible en `http://localhost:8080`.

## Estructura del backend

```text
src/main/java/pe/edu/upc/femfitai/
├── FemFitAiApplication.java   # Punto de entrada
├── controllers/              # Endpoints REST
├── services/                 # Interfaces, lógica y validaciones
├── repositories/             # Acceso a datos y consultas JPA
├── entities/                 # Mapeo de las tablas
└── dtos/                     # Datos de entrada y respuesta
src/main/resources/
└── application.properties.example
src/test/                     # Pruebas
.mvn/                         # Configuración del Maven Wrapper
pom.xml                       # Dependencias y compilación
```

Los módulos REST actuales se exponen en `/usuarios`, `/rutinas`, `/ciclos` y `/sesiones`. El proyecto todavía no incorpora Spring Security.

## Antes de publicar en GitHub

Comprueba que la configuración local está ignorada y no está en el índice:

```sh
git check-ignore -v src/main/resources/application.properties
git ls-files -- src/main/resources/application.properties
git status --short
```

El primer comando debe mostrar la regla de `.gitignore` y el segundo no debe devolver ninguna ruta. Se excluyen también archivos de IDE, compilados, `target/`, logs y variables de entorno. Conserva en el repositorio `pom.xml`, `mvnw`, `mvnw.cmd` y la configuración del wrapper en `.mvn/`.
