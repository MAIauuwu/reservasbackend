FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Copy sources and build
COPY pom.xml mvnw README.md ./
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /workspace/target/reserva-backend-0.0.1-SNAPSHOT.jar ./reserva-backend.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/reserva-backend.jar"]
