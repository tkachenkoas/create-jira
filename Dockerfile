FROM amazoncorretto:17-alpine

# Install curl
RUN apk update && apk add curl

# Download release JAR file
ARG JAR_URL=https://github.com/tkachenkoas/create-jira/releases/download/0.1.5/create-jira.jar
RUN curl -L -o /app/create-jira.jar $JAR_URL

# Run the JAR file
CMD ["java", "-jar", "create-jira.jar"]