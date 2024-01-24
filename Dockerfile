FROM ghcr.io/navikt/baseimages/temurin:21

COPY app/target/app.jar /app/app.jar
COPY export-vault-secrets.sh /init-scripts/10-export-vault-secrets.sh

ENV JAVA_OPTS="-Dspring.profiles.active=nais -Xmx2048m"