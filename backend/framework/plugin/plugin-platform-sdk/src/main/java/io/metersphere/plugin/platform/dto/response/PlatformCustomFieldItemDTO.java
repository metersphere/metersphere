package io.metersphere.plugin.platform.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class PlatformCustomFieldItemDTO extends PlatformCustomFieldDTO {
    private Object value;
    private String key;
    private String customData;
    private Boolean required;
    private String defaultValue;
    private Boolean supportSearch;
    private String searchMethod;
    private String placeHolder;
    private Boolean systemField;
}
