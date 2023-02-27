package io.metersphere.dto;

import lombok.Data;

@Data
public class CustomFieldItemDTO {
    private String id;
    private String name;
    private Object value;
    private String type;
    private String key;
    private String customData;
    private String optionLabel;
}
