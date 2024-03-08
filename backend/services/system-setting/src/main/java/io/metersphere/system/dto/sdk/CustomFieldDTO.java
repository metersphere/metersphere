package io.metersphere.system.dto.sdk;

import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.CustomFieldOption;
import lombok.Data;

import java.util.List;

@Data
public class CustomFieldDTO extends CustomField {
    private List<CustomFieldOption> options;
    private boolean used;
}
