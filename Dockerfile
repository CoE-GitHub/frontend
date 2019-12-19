##
# Build container with Gradle
##
FROM openjdk:14-slim as builder
WORKDIR /app
COPY ["build.gradle", "gradlew", "./"]
COPY gradle gradle
RUN chmod +x gradlew
COPY . .
RUN ./gradlew build

##
# Serving container
##
FROM openjdk:14-slim
WORKDIR /app
COPY --from=builder /app .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/build/libs/frontend-0.1.0.jar"]
