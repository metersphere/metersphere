FROM metersphere/fabric8-java-alpine-openjdk11-jre

LABEL maintainer="FIT2CLOUD <support@fit2cloud.com>"

ARG MS_VERSION=dev
ARG DEPENDENCY=backend/target/dependency

COPY ${DEPENDENCY}/BOOT-INF/lib /opt/lib
COPY ${DEPENDENCY}/META-INF /opt/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /opt

# html 文件
COPY frontend/dist/*.html /opt/public/

# 静态文件
COPY frontend/dist/favicon.ico /opt/static/favicon.ico
COPY frontend/dist/fonts /opt/static/fonts
COPY frontend/dist/img /opt/static/img
COPY frontend/dist/js /opt/static/js
COPY frontend/dist/css /opt/static/css
COPY frontend/dist/*.worker.js /opt/static/
COPY frontend/src/assets/theme/index.css /opt/static/css/index.css

ENV JAVA_CLASSPATH=/opt:/opt/lib/ms-jmeter-core.jar:/opt/lib/*
ENV JAVA_MAIN_CLASS=io.metersphere.Application
ENV AB_OFF=true
ENV MS_VERSION=${MS_VERSION}
ENV JAVA_OPTIONS="-Dfile.encoding=utf-8 -Djava.awt.headless=true --add-opens java.base/jdk.internal.loader=ALL-UNNAMED"

RUN mv /opt/lib/ms-jmeter-core*.jar /opt/lib/ms-jmeter-core.jar

CMD ["/deployments/run-java.sh"]
