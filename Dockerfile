FROM eclipse-temurin:17-jdk AS builder
WORKDIR /build
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S app && adduser -S app -G app
USER app:app
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8083 9091
ENTRYPOINT ["java", "-Duser.timezone=America/Bogota", "-jar", "app.jar"]
