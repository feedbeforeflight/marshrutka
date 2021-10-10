FROM openjdk:18-jdk-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG JAR_FILE=target/marshrutka.jar
#ARG PROP_FILE=./database.properties

WORKDIR /opt/marshrutka
COPY ${JAR_FILE} marshrutka.jar

WORKDIR /opt/marshrutka/config
#COPY ${PROP_FILE} database.properties

ENTRYPOINT ["java","-jar","/opt/marshrutka/marshrutka.jar"]