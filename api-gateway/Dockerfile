FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY target/api-gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
