# ---------- Build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# -Dmaven.test.skip=true omite también la COMPILACIÓN de los tests (-DskipTests solo omite su ejecución)
RUN mvn -B -q clean package -Dmaven.test.skip=true

# ---------- Run ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/FemFitAI-0.0.1-SNAPSHOT.jar femfitai.jar
# Render inyecta PORT; la app lo lee en server.port=${PORT:8080}
EXPOSE 8080
# Limita la memoria de la JVM para no pasar el límite del plan gratuito (512 MB)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-XX:+UseSerialGC", "-jar", "femfitai.jar"]

