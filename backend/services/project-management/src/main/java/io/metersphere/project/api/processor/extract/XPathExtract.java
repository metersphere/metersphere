package io.metersphere.project.api.processor.extract;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.sdk.valid.EnumValue;
import lombok.Data;

/**
 * XPath提取
 */
@Data
@JsonTypeName("X_PATH")
public class XPathExtract extends ResultMatchingExtract {
    /**
     * 提取范围
     * 取值参考 {@link ResponseFormat}
     */
    @EnumValue(enumClass = ResponseFormat.class)
    private String responseFormat;

    public enum ResponseFormat {
        /**
         * XML
         */
        XML,
        /**
         * HTML
         */
        HTML
    }
}