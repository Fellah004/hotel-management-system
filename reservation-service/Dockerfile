FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY target/reservation-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app.jar"]
