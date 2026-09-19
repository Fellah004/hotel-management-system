FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY target/inventory-service-*.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "/app.jar"]
