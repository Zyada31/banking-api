# the base image
FROM amazoncorretto:17

WORKDIR /app

COPY target/*.jar app.jar

# Expose the application port
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseContainerSupport", "-XX:ActiveProcessorCount=2", "-XX:-UsePerfData", "-Dmicrometer.metrics.use-system-metrics=false", "-jar", "app.jar"]
