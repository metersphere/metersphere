FROM registry.cn-qingdao.aliyuncs.com/metersphere/alpine-openjdk11-jre

LABEL maintainer="FIT2CLOUD <support@fit2cloud.com>"

ARG MS_VERSION=dev
ARG DEPENDENCY=backend/target/dependency

COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

# html 文件
COPY frontend/dist/*.html /app/public/

# 静态文件
COPY frontend/dist/favicon.ico /app/static/favicon.ico
COPY frontend/dist/fonts /app/static/fonts
COPY frontend/dist/img /app/static/img
COPY frontend/dist/js /app/static/js
COPY frontend/dist/css /app/static/css
COPY frontend/dist/*.worker.js /app/static/
COPY frontend/src/assets/theme/index.css /app/static/css/index.css


RUN mv /app/lib/ms-jmeter-core-*.jar /app/lib/ms-jmeter-core.jar
RUN mv /app/jmeter /opt/

ENV JAVA_CLASSPATH=/app:/app/lib/ms-jmeter-core.jar:/opt/jmeter/lib/ext/*:/app/lib/*
ENV JAVA_MAIN_CLASS=io.metersphere.Application
ENV AB_OFF=true
ENV MS_VERSION=${MS_VERSION}
ENV JAVA_OPTIONS="-Dfile.encoding=utf-8 -Djava.awt.headless=true --add-opens java.base/jdk.internal.loader=ALL-UNNAMED"


CMD ["/deployments/run-java.sh"]
