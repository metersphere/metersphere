FROM metersphere/fabric8-java-alpine-openjdk8-jre

LABEL maintainer="FIT2CLOUD <support@fit2cloud.com>"

ARG MS_VERSION=v1.18
ARG DEPENDENCY=backend/target/dependency

COPY ${DEPENDENCY}/BOOT-INF/lib/ms-jmeter-core-* /opt/lib/ms-jmeter-core.jar
COPY ${DEPENDENCY}/BOOT-INF/lib/[^ms-jmeter-core]* /opt/lib
COPY ${DEPENDENCY}/META-INF /opt/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /opt

ENV JAVA_CLASSPATH=/opt:/opt/lib/ms-jmeter-core-*.jar:/opt/lib/*
ENV JAVA_MAIN_CLASS=io.metersphere.Application
ENV AB_OFF=true
ENV MS_VERSION=${MS_VERSION}
ENV JAVA_OPTIONS="-Dfile.encoding=utf-8 -Djava.awt.headless=true"

CMD ["/deployments/run-java.sh"]
