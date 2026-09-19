FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY target/staff-service-*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "/app.jar"]
