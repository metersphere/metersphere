package io.metersphere.commons.constants;

/**
 * Author: chunxing
 * Date: 2018/6/26  下午3:44
 * Description:
 */
public interface ParamConstants {

    String getValue();

    enum KeyCloak implements ParamConstants {

        USERNAME("keycloak.username"),
        PASSWORD("keycloak.password"),
        REALM("keycloak.realm"),
        AUTH_SERVER_URL("keycloak.auth-server-url"),
        ADDRESS("keycloak-server-address");

        private String value;

        KeyCloak(String value) {
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

        KEYCLOAK("keycloak"),
        MAIL("smtp"),
        UI("ui"),
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

    enum UI implements ParamConstants {

        LOGO("ui.logo"),
        SYSTEM_NAME("ui.system.name"),
        THEME_PRIMARY("ui.theme.primary"),
        THEME_ACCENT("ui.theme.accent"),
        FAVICON("ui.favicon"),
        LOGIN_TITLE("ui.login.title"),
        LOGIN_IMG("ui.login.img"),
        SUPPORT_NAME("ui.support.name"),
        SUPPORT_URL("ui.support.url"),
        TITLE("ui.title");

        private String value;

        UI(String value) {
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

    enum MAIL {
        SERVER("smtp.server", 1),
        PORT("smtp.port", 2),
        ACCOUNT("smtp.account", 3),
        PASSWORD("smtp.password", 4),
        SSL("smtp.ssl", 5),
        TLS("smtp.tls", 6);

        private String key;
        private Integer value;

        MAIL(String key, Integer value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Integer getValue() {
            return value;
        }
    }

    enum Log implements ParamConstants {
        KEEP_MONTHS("log.keep.months");
        private String value;

        Log(String value) {
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
