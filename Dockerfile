FROM openjdk:11-jre-slim

COPY . /usr/src/app
WORKDIR /usr/src/app

RUN chmod +x ./gradlew

RUN ./gradlew jar

CMD ["java", "-jar", "build/libs/create-jira.jar"]