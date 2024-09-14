# Even though this app is meant to be built with the included
# Gradle Wrapper, this is gradle image(instead of 'openjdk:17-jdk-slim-bullseye')
# is actually faster since it doesn't have
# to download this gradle distribution every time you build it.
FROM gradle:7.6.4-jdk17 AS builder
COPY . /project
WORKDIR /project
RUN gradle bootJar

FROM openjdk:17-jdk-slim-bullseye
ARG JAR_FILE=/project/build/libs/*.jar
COPY --from=builder ${JAR_FILE} ./application.jar
ENV SPRING_PROFILES_ACTIVE=tasks-lock-api
ENTRYPOINT ["java", "-jar", "application.jar"]