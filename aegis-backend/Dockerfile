# --- Stage 1: Build ---
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copia apenas o pom.xml primeiro para cachear dependências (layer caching)
COPY pom.xml .
# Baixa dependências (sem copiar o código fonte ainda)
RUN mvn dependency:go-offline

# Copia o código fonte e compila
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Cria usuário não-root para segurança (boas práticas de prod)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia o JAR do stage de build
COPY --from=builder /app/target/*.jar app.jar

# Configurações de execução
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
