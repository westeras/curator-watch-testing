FROM openjdk:8-jre
MAINTAINER Adam Westerman <aswest92@gmail.com>

ARG JAR_FILE

WORKDIR /zk-auto-writer

COPY /src/main/docker/scripts /scripts
COPY $JAR_FILE zk-auto-writer.jar

RUN chmod +x /scripts/start-instance.sh

ENTRYPOINT ["/scripts/start-instance.sh"]