package io.metersphere.api.dto.definition.request.extract;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class MsExtractCommon extends MsExtractType{
    private String variable;
    private String value;
    private String expression;
    private String description;
    private boolean multipleMatching;

    public boolean isValid() {
        return StringUtils.isNotBlank(variable) && StringUtils.isNotBlank(expression);
    }
}
