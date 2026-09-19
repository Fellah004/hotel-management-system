FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY target/rate-service-*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "/app.jar"]
