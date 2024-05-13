FROM openjdk:17-ea-jdk-slim
VOLUME /tmp
COPY build/libs/eday-server-0.0.1-SNAPSHOT.jar eday-server.jar
ENTRYPOINT ["java", "-jar", "eday-server.jar"]
