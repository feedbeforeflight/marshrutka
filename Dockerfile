FROM openjdk:18-jdk-alpine

RUN mkdir /opt/marshrutka
RUN mkdir /opt/marshrutka/config

RUN addgroup -S spring && adduser -S spring -G spring
#RUN groupadd -r app && useradd -r -gapp app
USER spring:spring

ARG JAR_FILE=target/marshrutka.jar
#ARG PROP_FILE=./database.properties

WORKDIR /opt/marshrutka
COPY ${JAR_FILE} marshrutka.jar

WORKDIR /opt/marshrutka/config
#COPY ${PROP_FILE} database.properties

EXPOSE 8080
VOLUME /opt/marshrutka/config

ENTRYPOINT ["java","-jar","/opt/marshrutka/marshrutka.jar"]