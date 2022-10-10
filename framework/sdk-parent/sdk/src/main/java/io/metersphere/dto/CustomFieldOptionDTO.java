package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomFieldOptionDTO {
    private String value;
    private String text;
    private Boolean system = false;

    public CustomFieldOptionDTO(String value, String text, Boolean system) {
        this.text = text;
        this.value = value;
        this.system = system;
    }

    public CustomFieldOptionDTO() {}
}
