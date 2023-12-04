FROM ghcr.io/navikt/baseimages/temurin:21

COPY app/target/app.jar /app/app.jar

ENV JAVA_OPTS="-Dspring.profiles.active=nais"