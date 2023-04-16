package io.metersphere.plugin.platform.dto;

import lombok.Data;

@Data
public class PlatformCustomFieldItemDTO extends CustomFieldDTO {
    private Object value;
    private String key;
    private String customData;
    private Boolean required;
    private String defaultValue;
}
