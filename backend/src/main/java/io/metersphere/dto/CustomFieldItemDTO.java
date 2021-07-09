package io.metersphere.dto;

import lombok.Data;

@Data
public class CustomFieldItemDTO {
    private String id;
    private String name;
    private String value;
    private String type;
    private String customData;
}
