FROM openjdk:8-jre-alpine
COPY target/bara-midlara.jar /opt
COPY demo.config /
COPY th*.json /
ENTRYPOINT ["java", "-jar", "/opt/bara-midlara.jar"]
