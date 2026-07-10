# Etapa 1: compilar el proyecto con Java 17 y Maven
FROM maven:3.9.9-eclipse-temurin-17 AS buildstage

WORKDIR /app

# Copiar el pom y descargar las dependencias
COPY pom.xml .

RUN mvn -B dependency:go-offline

# Copiar el codigo fuente y generar el archivo JAR
COPY src ./src

RUN mvn -B clean package -DskipTests

# Etapa 2: ejecutar la aplicacion
FROM eclipse-temurin:17-jre

WORKDIR /app

# Directorios que se montaran desde EC2
RUN mkdir -p /app/efs /app/wallet

COPY --from=buildstage /app/target/microservicio-1.0.0.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
