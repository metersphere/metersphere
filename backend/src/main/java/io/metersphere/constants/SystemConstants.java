package io.metersphere.constants;

/**
 * 系统级别通用的常量和枚举
 */
public class SystemConstants {

    public enum TestTypeEnum {
        API("API"),
        UI("UI"),
        PERFORMANCE("PERFORMANCE");
        private String name;

        TestTypeEnum(String name) {
            this.name = name;
        }
    }
}
