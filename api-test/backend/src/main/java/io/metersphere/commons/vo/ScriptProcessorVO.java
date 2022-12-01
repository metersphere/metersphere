package io.metersphere.commons.vo;

import lombok.Data;

@Data
public class ScriptProcessorVO {
    private String name;
    private String scriptLanguage;
    private String script;
    private Boolean jsrEnable;
    private boolean enabled;
    private String environmentId;
}
