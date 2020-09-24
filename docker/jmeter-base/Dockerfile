FROM alpine:latest
LABEL maintainer="support@fit2cloud.com"

ENV JMETER_VERSION "5.3"
ENV KAFKA_BACKEND_LISTENER_VERSION "1.0.4"
#定义时区参数
ENV TZ=Asia/Shanghai

RUN apk update && \
    apk upgrade && \
    apk add --update openjdk8 wget tar bash && \
    wget https://mirrors.tuna.tsinghua.edu.cn/apache/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz && \
    wget https://jmeter-plugins.org/files/packages/jpgc-casutg-2.9.zip && \
    wget https://jmeter-plugins.org/files/packages/jpgc-tst-2.5.zip && \
    wget https://github.com/metersphere/jmeter-backend-listener-kafka/releases/download/v${KAFKA_BACKEND_LISTENER_VERSION}/jmeter.backendlistener.kafka-${KAFKA_BACKEND_LISTENER_VERSION}.jar && \
    wget https://github.com/metersphere/jmeter-plugins-for-apache-dubbo/releases/download/2.7.7/jmeter-plugins-dubbo-2.7.7-jar-with-dependencies.jar && \
    wget -q "http://search.maven.org/remotecontent?filepath=mysql/mysql-connector-java/5.1.49/mysql-connector-java-5.1.49.jar" -O mysql-connector-java.jar && \
    wget -q "http://search.maven.org/remotecontent?filepath=com/oracle/database/jdbc/ojdbc8/19.7.0.0/ojdbc8-19.7.0.0.jar" -O ojdbc8.jar && \
    wget -q "http://search.maven.org/remotecontent?filepath=org/postgresql/postgresql/42.2.14/postgresql-42.2.14.jar" -O postgresql.jar && \
    wget -q "http://search.maven.org/remotecontent?filepath=com/microsoft/sqlserver/mssql-jdbc/7.4.1.jre8/mssql-jdbc-7.4.1.jre8.jar" -O mssql-jdbc.jar && \
    mkdir -p /opt/jmeter && \
    tar -zxf apache-jmeter-${JMETER_VERSION}.tgz -C /opt/jmeter/  --strip-components=1 && \
    unzip -o jpgc-casutg-2.9.zip -d /tmp/ && mv /tmp/lib/ext/jmeter-plugins-casutg-2.9.jar /opt/jmeter/lib/ext && \
    unzip -o jpgc-tst-2.5.zip -d /tmp/ && mv /tmp/lib/ext/jmeter-plugins-tst-2.5.jar /opt/jmeter/lib/ext && \
    mv jmeter.backendlistener.kafka-${KAFKA_BACKEND_LISTENER_VERSION}.jar /opt/jmeter/lib/ext && \
    mv jmeter-plugins-dubbo-2.7.7-jar-with-dependencies.jar /opt/jmeter/lib/ext && \
    mv mysql-connector-java.jar /opt/jmeter/lib/ext && \
    mv ojdbc8.jar /opt/jmeter/lib/ext && \
    mv postgresql.jar /opt/jmeter/lib/ext && \
    mv mssql-jdbc.jar /opt/jmeter/lib/ext && \
    rm -rf apache-jmeter-${JMETER_VERSION}.tgz && \
    rm -rf jpgc-casutg-2.9.zip && \
    rm -rf jpgc-tst-2.5.zip && \
    rm -rf jmeter.backendlistener.kafka-${KAFKA_BACKEND_LISTENER_VERSION}.jar && \
    rm -rf jmeter-plugins-dubbo-2.7.7-jar-with-dependencies.jar && \
    rm -rf /var/cache/apk/* && \
    wget -O /usr/bin/tpl https://github.com/schneidexe/tpl/releases/download/v0.5.0/tpl-linux-amd64 && \
    chmod +x /usr/bin/tpl && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo "$TZ" > /etc/timezone

ENV JMETER_HOME /opt/jmeter
ENV PATH $PATH:$JMETER_HOME/bin:/usr/lib/jvm/java-1.8-openjdk/bin

ADD log4j2.xml $JMETER_HOME/bin/log4j2.xml
ADD jmeter.properties $JMETER_HOME/bin/jmeter.properties