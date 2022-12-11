package io.metersphere.dto;

import io.metersphere.platform.domain.SelectOption;
import lombok.Data;

import java.util.List;

@Data
public class ThirdPartIssueField {
    private String name;
    private String type;
    private String defaultValue;
    private boolean required;
    private String optionMethod;
    private List<SelectOption> options;
    private String label;
    private String message;
}
