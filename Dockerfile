FROM metersphere/fabric8-java-alpine-openjdk8-jre

LABEL maintainer="FIT2CLOUD <support@fit2cloud.com>"

ARG MS_VERSION=dev
ARG DEPENDENCY=backend/target/dependency

COPY ${DEPENDENCY}/BOOT-INF/lib /opt/lib
COPY ${DEPENDENCY}/META-INF /opt/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /opt

ENV JAVA_CLASSPATH=/opt:/opt/lib/ms-jmeter-core.jar:/opt/lib/*
ENV JAVA_MAIN_CLASS=io.metersphere.Application
ENV AB_OFF=true
ENV MS_VERSION=${MS_VERSION}
ENV JAVA_OPTIONS="-Dfile.encoding=utf-8 -Djava.awt.headless=true"

CMD ["sh", "-c", "mv /opt/lib/ms-jmeter-core-*.jar /opt/lib/ms-jmeter-core.jar && /deployments/run-java.sh"]
