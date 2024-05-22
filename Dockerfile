FROM openjdk:17-ea-jdk-slim
VOLUME /tmp
COPY build/libs/eday-server-0.0.1-SNAPSHOT.jar eday-server.jar
ENV NEO4J_URI=$NEO4J_URI
ENV NEO4J_PW=$NEO4J_PW
ENTRYPOINT ["java", "-jar", "eday-server.jar"]
