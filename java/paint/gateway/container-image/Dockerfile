FROM openjdk:8u191-alpine
EXPOSE 4545
RUN mkdir /app
WORKDIR /app
COPY target/dependency/paint.gateway-service-0.0.1-SNAPSHOT.jar /app/paint.gateway-service-0.0.1-SNAPSHOT.jar
CMD java ${JAVA_OPTS} -jar /app/paint.gateway-service-0.0.1-SNAPSHOT.jar
