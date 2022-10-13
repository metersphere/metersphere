FROM registry.cn-qingdao.aliyuncs.com/metersphere/alpine-openjdk11-jre

LABEL maintainer="FIT2CLOUD <support@fit2cloud.com>"

ARG MS_VERSION=dev
ARG DEPENDENCY=target/dependency

COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

# html 文件
COPY src/main/resources/static/*.html /app/public/

# 静态文件
COPY src/main/resources/static/favicon.ico /app/static/favicon.ico
COPY src/main/resources/static/fonts /app/static/fonts
COPY src/main/resources/static/img /app/static/img
COPY src/main/resources/static/js /app/static/js
COPY src/main/resources/static/css /app/static/css
COPY src/main/resources/static/*.worker.js /app/static/


ENV JAVA_CLASSPATH=/app:/app/lib/*
ENV JAVA_MAIN_CLASS=io.metersphere.gateway.GatewayApplication
ENV MS_VERSION=${MS_VERSION}
ENV JAVA_OPTIONS="-Dfile.encoding=utf-8 -Djava.awt.headless=true --add-opens java.base/jdk.internal.loader=ALL-UNNAMED -Dreactor.netty.pool.leasingStrategy=lifo"


CMD ["/deployments/run-java.sh"]
