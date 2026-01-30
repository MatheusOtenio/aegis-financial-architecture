# Estágio de Build (Maven)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compila e gera o JAR (pulando testes para agilizar build de imagem, testes já rodaram na CI)
RUN mvn clean package -DskipTests

# Estágio Final (Runtime Leve)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Cria usuário não-root para segurança
RUN addgroup -S aegis && adduser -S aegis -G aegis
USER aegis:aegis

# Copia o JAR do estágio de build
COPY --from=build /app/target/*.jar app.jar

# Configurações padrão de JVM
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Timezone (Crucial para o job rodar na hora certa e D-1 ser calculado corretamente)
ENV TZ="America/Sao_Paulo"

# Expõe porta (apenas documentação, spring usa 8081)
EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
