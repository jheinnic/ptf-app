FROM openjdk:8u191-alpine
EXPOSE 4545
RUN mkdir /app
WORKDIR /app
COPY target/dependency/ecosystem.action-service-0.0.1-SNAPSHOT.jar /app/ecosystem.action-service-0.0.1-SNAPSHOT.jar
CMD java ${JAVA_OPTS} -jar /app/ecosystem.action-service-0.0.1-SNAPSHOT.jar
