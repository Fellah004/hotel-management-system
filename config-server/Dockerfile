FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY target/config-server-*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app.jar"]
