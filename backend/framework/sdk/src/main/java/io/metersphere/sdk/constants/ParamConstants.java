package io.metersphere.sdk.constants;


public interface ParamConstants {

    String getValue();

    enum Classify implements ParamConstants {
        MAIL("smtp"),
        BASE("base"),
        LDAP("ldap"),
        REGISTRY("registry");

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
        TLS("smtp.tls"),
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

}
