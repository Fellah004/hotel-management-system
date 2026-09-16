FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY target/guest-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app.jar"]
