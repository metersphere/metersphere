package io.metersphere.plugin.platform.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class SelectOption {
    public SelectOption(String text, String value) {
        this.text = text;
        this.value = value;
    }

    private String text;
    private String value;
}
