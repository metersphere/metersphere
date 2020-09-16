FROM registry.fit2cloud.com/metersphere/jmeter-base:0.0.1
LABEL maintainer="support@fit2cloud.com"

EXPOSE 60000
ENV SSL_DISABLED true
ENV TESTS_DIR /test

ADD run-test.sh /run-test.sh
RUN chmod +x /run-test.sh \
    && mkdir /test \
    && mkdir /jmeter-log

WORKDIR /jmeter-log/
ENTRYPOINT /run-test.sh