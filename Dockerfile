FROM gradle:7.6.4-jdk17 AS builder
COPY . /project
WORKDIR /project
RUN gradle bootJar

FROM openjdk:17-jdk-slim-bullseye
ARG JAR_FILE=/project/build/libs/*.jar
COPY --from=builder ${JAR_FILE} ./application.jar
ENV TZ=America/Chicago
ENV TASKS_LOCK_API_ENABLED=true
ENV SPRING_PROFILES_ACTIVE=tasks-lock-api
ENTRYPOINT ["java", "-jar", "application.jar"]