FROM openjdk:18-jdk-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG JAR_FILE=target/marshrutka.jar
ARG PROP_FILE=./database.properties
COPY ${JAR_FILE} marshrutka.jar
COPY ${PROP_FILE} database.properties
ENTRYPOINT ["java","-jar","/marshrutka.jar"]