FROM openjdk:18-jdk-alpine

ARG JAR_FILE=target/marshrutka.jar
#ARG PROP_FILE=./database.properties

RUN mkdir /opt/marshrutka
WORKDIR /opt/marshrutka
COPY ${JAR_FILE} marshrutka.jar

RUN mkdir /opt/marshrutka/config
WORKDIR /opt/marshrutka/config
#COPY ${PROP_FILE} database.properties

EXPOSE 8080
VOLUME /opt/marshrutka/config

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ENTRYPOINT ["java","-jar","/opt/marshrutka/marshrutka.jar"]