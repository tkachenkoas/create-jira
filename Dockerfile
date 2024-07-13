FROM openjdk:11-jre-slim

# Install curl
RUN apt-get update && apt-get install -y curl

# Set the working directory
WORKDIR /app

# Download release JAR file
ARG JAR_URL=https://github.com/tkachenkoas/create-jira/releases/download/0.1.4/create-jira.jar
RUN curl -L -o /app/create-jira.jar $JAR_URL

# Run the JAR file
CMD ["java", "-jar", "create-jira.jar"]