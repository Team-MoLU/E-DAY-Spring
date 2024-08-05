FROM openjdk:17-ea-jdk-slim
VOLUME /tmp
COPY build/libs/eday-server-0.0.1-SNAPSHOT.jar eday-server.jar
ENV NEO4J_URI=$NEO4J_URI
ENV NEO4J_PW=$NEO4J_PW
ENV CLIENT_URL=https://eday.site
ENV SERVER_URL=https://eday.site
ENTRYPOINT ["java", "-jar", "eday-server.jar"]
