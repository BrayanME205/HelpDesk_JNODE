# imagen oficial de Java 21 para compilar
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copiar los archivos de Maven del proyecto
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

# Copiar el código fuente y compilar saltándose los tests de Selenium
COPY src ./src
RUN ./mvnw clean package -DskipTests

# crear la imagen ligera para correr la app
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/demo-1.0.0.jar app.jar

# puerto que usa la app e iniciarla
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]