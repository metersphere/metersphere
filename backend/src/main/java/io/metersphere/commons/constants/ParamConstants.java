package io.metersphere.commons.constants;


public interface ParamConstants {

    String getValue();

    enum Type implements ParamConstants {

        PASSWORD("password"),
        TEXT("text"),
        JSON("json");

        private String value;

        Type(String value) {
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

    enum Classify implements ParamConstants {

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


    enum Registry implements ParamConstants {
        URL("registry.url"),
        REPO("registry.repo"),
        USERNAME("registry.username"),
        PASSWORD("registry.password");

        private String value;

        Registry(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    enum I18n implements ParamConstants {

        LANGUAGE("i18n.language");

        private String value;

        I18n(String value) {
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
}
