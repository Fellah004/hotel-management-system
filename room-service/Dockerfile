FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY target/room-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app.jar"]
