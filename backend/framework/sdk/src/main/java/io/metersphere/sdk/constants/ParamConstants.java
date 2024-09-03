package io.metersphere.sdk.constants;


public interface ParamConstants {

    String getValue();

    enum Classify implements ParamConstants {
        MAIL("smtp"),
        BASE("base"),
        LDAP("ldap"),
        REGISTRY("registry"),
        CLEAN_CONFIG("cleanConfig.operation"),
        UPLOAD_CONFIG("upload");

        private String value;

        Classify(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    enum BASE implements ParamConstants {
        URL("base.url"),
        PROMETHEUS_HOST("base.prometheus.host");

        private String value;

        private BASE(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    enum MAIL implements ParamConstants {
        SERVER("smtp.host"),
        PORT("smtp.port"),
        ACCOUNT("smtp.account"),
        FROM("smtp.from"),
        PASSWORD("smtp.password"),
        SSL("smtp.ssl"),
        TSL("smtp.tsl"),
        RECIPIENTS("smtp.recipient");

        private String value;

        private MAIL(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return this.value;
        }
    }

    enum CleanConfig implements ParamConstants {
        OPERATION_LOG("cleanConfig.operation.log"),
        OPERATION_HISTORY("cleanConfig.operation.history");

        private String value;

        private CleanConfig(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    enum ApiConcurrentConfig implements ParamConstants {
        API_CONCURRENT_CONFIG("api.concurrent.config");

        private String value;

        private ApiConcurrentConfig(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    enum UploadConfig implements ParamConstants {
        UPLOAD_FILE_SIZE("upload.file.size");

        private String value;

        private UploadConfig(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }
}
