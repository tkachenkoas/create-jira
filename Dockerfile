FROM amazoncorretto:17-alpine

# Install curl
RUN apk update && apk add curl

# Download release JAR file
ARG JAR_URL=https://github.com/tkachenkoas/create-jira/releases/download/1.0.0/create-jira.jar
RUN curl -L -o /create-jira.jar $JAR_URL

# Run the JAR file
ENTRYPOINT  ["java", "-jar", "/create-jira.jar"]