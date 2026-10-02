FROM maven:3.9.16-eclipse-temurin-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
COPY --from=build /target/FemFitAI-0.0.1-SNAPSHOT.jar femfitai.jar
EXPOSE 8085
ENTRYPOINT ["java","-jar","femfitai.jar"]
