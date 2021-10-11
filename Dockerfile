FROM openjdk:18-jdk-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG JAR_FILE=target/marshrutka.jar
#ARG PROP_FILE=./database.properties

RUN MKDIR /opt/marshrutka
WORKDIR /opt/marshrutka
COPY ${JAR_FILE} marshrutka.jar

RUN MKDIR /opt/marshrutka/config
WORKDIR /opt/marshrutka/config
#COPY ${PROP_FILE} database.properties

EXPOSE 8080
VOLUME /opt/marshrutka/config

ENTRYPOINT ["java","-jar","/opt/marshrutka/marshrutka.jar"]