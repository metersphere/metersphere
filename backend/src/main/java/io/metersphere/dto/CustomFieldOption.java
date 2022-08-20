package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomFieldOption {
    private String value;
    private String text;
    private Boolean system = false;

    public CustomFieldOption(String value, String text, Boolean system) {
        this.text = text;
        this.value = value;
        this.system = system;
    }

    public CustomFieldOption() {}
}
