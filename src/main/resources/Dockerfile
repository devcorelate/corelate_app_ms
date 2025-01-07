FROM openjdk:11-jre-slim
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-javaagent:/opentelemetry-javaagent.jar","-jar","/app.jar"]
