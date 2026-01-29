# Etapa de construcción
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

# Copiamos Gradle wrapper y archivos de build para caché.
COPY gradlew build.gradle settings.gradle /workspace/
COPY gradle /workspace/gradle

# Descargamos dependencias.
RUN ./gradlew --no-daemon dependencies

# Copiamos el código fuente y compilamos.
COPY src /workspace/src
RUN ./gradlew --no-daemon clean bootJar

# Etapa de ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiamos el JAR construido.
COPY --from=builder /workspace/build/libs/*.jar /app/finances-app.jar

EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java", "-jar", "/app/finances-app.jar"]
