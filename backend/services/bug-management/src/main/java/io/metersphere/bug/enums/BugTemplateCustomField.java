package io.metersphere.bug.enums;

import io.metersphere.sdk.util.Translator;
import lombok.Getter;

@Getter
public enum BugTemplateCustomField {

    /**
     * 处理人
     */
    HANDLE_USER("handleUser", Translator.get("bug.handle_user")),
    /**
     * 状态
     */
    STATUS("status", Translator.get("bug.status"));


    private final String id;

    private final String name;

    BugTemplateCustomField(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
