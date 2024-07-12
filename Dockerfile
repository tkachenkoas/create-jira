FROM openjdk:11-jre-slim

COPY . /usr/src/app
WORKDIR /usr/src/app

RUN chmod +x gradlew

ENTRYPOINT ["./gradlew", "run"]
