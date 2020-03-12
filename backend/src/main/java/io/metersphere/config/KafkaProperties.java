package io.metersphere.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = KafkaProperties.KAFKA_PREFIX)
public class KafkaProperties {
    public static final String KAFKA_PREFIX = "kafka";

    private String acks;
    private String topic;
    private String fields;
    private String timestamp;
    private String bootstrapServers;
    private String sampleFilter;
    private String testMode;
    private String parseAllReqHeaders;
    private String parseAllResHeaders;
    private String compressionType;
    private String batchSize;
    private String clientId;
    private String connectionsMaxIdleMs;
    private KafkaProperties.Ssl ssl = new KafkaProperties.Ssl();

    public String getAcks() {
        return acks;
    }

    public void setAcks(String acks) {
        this.acks = acks;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getSampleFilter() {
        return sampleFilter;
    }

    public void setSampleFilter(String sampleFilter) {
        this.sampleFilter = sampleFilter;
    }

    public String getTestMode() {
        return testMode;
    }

    public void setTestMode(String testMode) {
        this.testMode = testMode;
    }


    public String getCompressionType() {
        return compressionType;
    }

    public void setCompressionType(String compressionType) {
        this.compressionType = compressionType;
    }


    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getParseAllReqHeaders() {
        return parseAllReqHeaders;
    }

    public void setParseAllReqHeaders(String parseAllReqHeaders) {
        this.parseAllReqHeaders = parseAllReqHeaders;
    }

    public String getParseAllResHeaders() {
        return parseAllResHeaders;
    }

    public void setParseAllResHeaders(String parseAllResHeaders) {
        this.parseAllResHeaders = parseAllResHeaders;
    }

    public String getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(String batchSize) {
        this.batchSize = batchSize;
    }

    public String getConnectionsMaxIdleMs() {
        return connectionsMaxIdleMs;
    }

    public void setConnectionsMaxIdleMs(String connectionsMaxIdleMs) {
        this.connectionsMaxIdleMs = connectionsMaxIdleMs;
    }

    public static class Ssl {
        private String enabled = "false";
        private String keyPassword;
        private String keystoreLocation;
        private String keystorePassword;
        private String keystoreType;
        private String truststoreLocation;
        private String truststorePassword;
        private String truststoreType;
        private String protocol;
        private String enabledProtocols;
        private String provider;

        public Ssl() {
        }

        public String getEnabled() {
            return enabled;
        }

        public void setEnabled(String enabled) {
            this.enabled = enabled;
        }

        public String getKeyPassword() {
            return keyPassword;
        }

        public void setKeyPassword(String keyPassword) {
            this.keyPassword = keyPassword;
        }

        public String getKeystoreLocation() {
            return keystoreLocation;
        }

        public void setKeystoreLocation(String keystoreLocation) {
            this.keystoreLocation = keystoreLocation;
        }

        public String getKeystorePassword() {
            return keystorePassword;
        }

        public void setKeystorePassword(String keystorePassword) {
            this.keystorePassword = keystorePassword;
        }

        public String getKeystoreType() {
            return keystoreType;
        }

        public void setKeystoreType(String keystoreType) {
            this.keystoreType = keystoreType;
        }

        public String getTruststoreLocation() {
            return truststoreLocation;
        }

        public void setTruststoreLocation(String truststoreLocation) {
            this.truststoreLocation = truststoreLocation;
        }

        public String getTruststorePassword() {
            return truststorePassword;
        }

        public void setTruststorePassword(String truststorePassword) {
            this.truststorePassword = truststorePassword;
        }

        public String getTruststoreType() {
            return truststoreType;
        }

        public void setTruststoreType(String truststoreType) {
            this.truststoreType = truststoreType;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public String getEnabledProtocols() {
            return enabledProtocols;
        }

        public void setEnabledProtocols(String enabledProtocols) {
            this.enabledProtocols = enabledProtocols;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }
    }

    public Ssl getSsl() {
        return ssl;
    }

    public void setSsl(Ssl ssl) {
        this.ssl = ssl;
    }
}
