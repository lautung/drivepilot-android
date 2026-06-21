FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY gradlew gradle.properties ./
COPY gradle ./gradle
COPY backend/build.gradle.kts backend/settings.gradle.kts ./backend/
COPY backend/src ./backend/src
RUN --mount=type=cache,target=/root/.gradle \
    chmod +x gradlew && ./gradlew -p backend bootJar --no-daemon

FROM eclipse-temurin:21-jre
RUN groupadd --system phonecar && useradd --system --gid phonecar --home-dir /app phonecar
USER phonecar
WORKDIR /app
COPY --from=build /workspace/backend/build/libs/phonecar-backend.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
