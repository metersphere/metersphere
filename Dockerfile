FROM registry.fit2cloud.com/metersphere/fabric8-java-alpine-openjdk8-jre

MAINTAINER FIT2CLOUD <support@fit2cloud.com>

ARG MS_VERSION=dev

RUN mkdir -p /opt/apps && mkdir -p /opt/jmeter

ADD backend/target/backend-1.3.jar /opt/apps

ADD backend/target/classes/jmeter/ /opt/jmeter/

ENV JAVA_APP_JAR=/opt/apps/backend-1.3.jar

ENV AB_OFF=true

ENV MS_VERSION=${MS_VERSION}

ENV JAVA_OPTIONS="-Dfile.encoding=utf-8 -Djava.awt.headless=true"
CMD ["/deployments/run-java.sh"]
