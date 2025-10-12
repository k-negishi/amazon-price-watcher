# Stage 1: Build the application
FROM gradle:8.2.1-jdk17 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
RUN gradle shadowJar --no-daemon

# Stage 2: Create the final lean image
FROM amazoncorretto:17-alpine-jre
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/app.jar .
CMD ["java", "-jar", "app.jar"]
